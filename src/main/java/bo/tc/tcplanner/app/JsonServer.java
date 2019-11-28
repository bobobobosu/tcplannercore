package bo.tc.tcplanner.app;

import bo.tc.tcplanner.datastructure.LocationHierarchyMap;
import bo.tc.tcplanner.datastructure.TimelineBlock;
import bo.tc.tcplanner.datastructure.ValueEntryMap;
import bo.tc.tcplanner.datastructure.converters.DataStructureWriter;
import bo.tc.tcplanner.domain.Schedule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static bo.tc.tcplanner.app.TCSchedulingApp.*;
import static bo.tc.tcplanner.app.Toolbox.printTimelineBlock;

public class JsonServer {
    private static final String HOSTNAME = "0.0.0.0";
    private static final int PORT = 8080;
    private static final int BACKLOG = 1;
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    //Solvers
    SolverThread solverThread;
    //Latest Solutions
    private TimelineBlock latestTimelineBlock;
    private TimelineBlock problemTimelineBlock;
    private Schedule latestBestSolutions;

    //Locks
    private Object resumeSolvingLock;
    private Object newTimelineBlockLock;


    public JsonServer() {
    }

    public static void main(final String... args) throws IOException {
        JsonServer jsonServer = new JsonServer();
        jsonServer.createServer().start();
    }

    public HttpServer createServer() throws IOException {
        final HttpServer server = HttpServer.create(new InetSocketAddress(HOSTNAME, PORT), BACKLOG);
        NewTimelineBlockNotifier newTimelineBlockNotifier = new NewTimelineBlockNotifier();
        server.createContext("/newTimelineBlock", newTimelineBlockNotifier);
        UpdateOptaFilesHandler updateOptaFilesHandler = new UpdateOptaFilesHandler();
        server.createContext("/updateOptaFiles", updateOptaFilesHandler);
        return server;
    }

    public void updateTimelineBlock(boolean print, Schedule newresult) {
        latestTimelineBlock = new DataStructureWriter().generateTimelineBlock(problemTimelineBlock, newresult);
        latestBestSolutions = newresult;
        synchronized (newTimelineBlockLock) {
            newTimelineBlockLock.notify();
        }
        if (print)
            printTimelineBlock(latestTimelineBlock);
    }

    public void saveFiles() {
        // Write TimelineBlock_result
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(fpath_TimelineBlock), latestTimelineBlock);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean compareTimelineBlock(TimelineBlock TB1, TimelineBlock TB2) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(TB1).equals(mapper.writeValueAsString(TB2));
    }

    private Map<String, List<String>> getRequestParameters(final URI requestUri) {
        final Map<String, List<String>> requestParameters = new LinkedHashMap<>();
        final String requestQuery = requestUri.getRawQuery();
        if (requestQuery != null) {
            final String[] rawRequestParameters = requestQuery.split("[&;]", -1);
            for (final String rawRequestParameter : rawRequestParameters) {
                final String[] requestParameter = rawRequestParameter.split("=", 2);
                final String requestParameterName = decodeUrlComponent(requestParameter[0]);
                requestParameters.putIfAbsent(requestParameterName, new ArrayList<>());
                final String requestParameterValue = requestParameter.length > 1
                        ? decodeUrlComponent(requestParameter[1])
                        : null;
                requestParameters.get(requestParameterName).add(requestParameterValue);
            }
        }
        return requestParameters;
    }

    private String decodeUrlComponent(final String urlComponent) {
        try {
            return URLDecoder.decode(urlComponent, CHARSET.name());
        } catch (final UnsupportedEncodingException ex) {
            throw new InternalError(ex);
        }
    }

    public TimelineBlock getLatestTimelineBlock() {
        return latestTimelineBlock;
    }

    public void setLatestTimelineBlock(TimelineBlock latestTimelineBlock) {
        this.latestTimelineBlock = latestTimelineBlock;
    }

    public Schedule getLatestBestSolutions() {
        return latestBestSolutions;
    }

    public void setLatestBestSolutions(Schedule latestBestSolutions) {
        this.latestBestSolutions = latestBestSolutions;
    }

    public Object getResumeSolvingLock() {
        return resumeSolvingLock;
    }

    public void setResumeSolvingLock(Object resumeSolvingLock) {
        this.resumeSolvingLock = resumeSolvingLock;
    }

    public SolverThread getSolverThread() {
        return solverThread;
    }

    public void setSolverThread(SolverThread solverThread) {
        this.solverThread = solverThread;
    }

    public Object getNewTimelineBlockLock() {
        return newTimelineBlockLock;
    }

    public void setNewTimelineBlockLock(Object newTimelineBlockLock) {
        this.newTimelineBlockLock = newTimelineBlockLock;
    }

    public TimelineBlock getProblemTimelineBlock() {
        return problemTimelineBlock;
    }

    public void setProblemTimelineBlock(TimelineBlock problemTimelineBlock) {
        this.problemTimelineBlock = problemTimelineBlock;
    }

    public enum StatusCode {
        OK(200), CREATED(201), ACCEPTED(202),

        BAD_REQUEST(400), METHOD_NOT_ALLOWED(405);

        private int code;

        StatusCode(final int newValue) {
            code = newValue;
        }

        public int getCode() {
            return code;
        }
    }

    public class NewTimelineBlockNotifier implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                throw new UnsupportedOperationException();
            }

            new Thread(() -> {
                //Wait
                synchronized (newTimelineBlockLock) {
                    try {
                        newTimelineBlockLock.wait();
                        System.out.println("Sending New TimelineBlock");
                        exchange.getResponseHeaders().set(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                        exchange.sendResponseHeaders(StatusCode.CREATED.getCode(), 0);
                        OutputStream responseBody = exchange.getResponseBody();
                        responseBody.write(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(latestTimelineBlock).getBytes(StandardCharsets.UTF_8));
                        responseBody.close();
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }

    public class UpdateOptaFilesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                throw new UnsupportedOperationException();
            }
            new Thread(() -> {
                try {
                    String javaString = URLDecoder.decode(IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8),
                            StandardCharsets.UTF_8);
                    Map<String, Map> updatedfiles = new ObjectMapper().readValue(javaString, Map.class);
                    for (Map.Entry<String, Map> entry : updatedfiles.entrySet()) {
                        if (entry.getKey().equals("TimeHierarchyMap.json")) {
                            timeHierarchyMap = new ObjectMapper().convertValue(entry.getValue(), HashMap.class);
                            System.out.println("TimeHierarchyMap Updated");
                        }
                        if (entry.getKey().equals("LocationHierarchyMap.json")) {
                            locationHierarchyMap = new ObjectMapper().convertValue(entry.getValue(), LocationHierarchyMap.class);
                            System.out.println("LocationHierarchyMap Updated");
                        }
                        if (entry.getKey().equals("ValueEntryMap.json")) {
                            valueEntryMap = new ObjectMapper().convertValue(entry.getValue(), ValueEntryMap.class);
                            System.out.println("ValueEntryMap Updated");
                        }
                        if (entry.getKey().equals("TimelineBlock.json")) {
                            TimelineBlock timelineBlock = new ObjectMapper().convertValue(entry.getValue(), TimelineBlock.class);
                            if (timelineBlock.getOrigin().equals("TCxlsb")) {
                                setProblemTimelineBlock(timelineBlock);
                                solverThread.restartSolversWithNewTimelineBlock(timelineBlock);
                            }
                            System.out.println("TimelineBlock Updated");
                        }
                    }

                    exchange.getResponseHeaders().
                            set(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                    exchange.sendResponseHeaders(StatusCode.CREATED.getCode(), 0);
                    OutputStream responseBody = exchange.getResponseBody();
                    responseBody.write("{\"Updated\":true}".getBytes(StandardCharsets.UTF_8));
                    responseBody.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();


        }
    }

    public class Constants {
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String APPLICATION_JSON = "application/json";
    }
}