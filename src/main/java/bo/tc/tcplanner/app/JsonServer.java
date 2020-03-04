package bo.tc.tcplanner.app;

import bo.tc.tcplanner.datastructure.*;
import bo.tc.tcplanner.datastructure.converters.DataStructureBuilder;
import bo.tc.tcplanner.datastructure.converters.DataStructureWriter;
import bo.tc.tcplanner.domain.Schedule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static bo.tc.tcplanner.app.TCSchedulingApp.*;
import static bo.tc.tcplanner.app.Toolbox.printCurrentSolution;

public class JsonServer {
    private static final String HOSTNAME = "0.0.0.0";
    private static final int PORT = 8080;
    private static final int BACKLOG = 1;
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    //Solvers
    SolverThread solverThread;
    FirebaseServer firebaseServer;
    //Latest Solutions
    private TimelineBlock problemTimelineBlock;
    private Schedule latestBestSolutions;

    //Locks
    private Object resumeSolvingLock;
    private Object newTimelineBlockLock;

    //Global
    public static StringBuffer consoleBuffer = new StringBuffer();

    public static String updateConsole(String s) {
        consoleBuffer.append(s).append("\n");
        return s;
    }

    public static String flushConsole() {
        String buff = consoleBuffer.toString();
        consoleBuffer.setLength(0);
        return buff;
    }

    public JsonServer() {
    }

    public static void main(final String... args) throws IOException {
        JsonServer jsonServer = new JsonServer();
        jsonServer.createServer().start();
    }

    public HttpServer createServer() throws IOException {
        final HttpServer server = HttpServer.create(new InetSocketAddress(HOSTNAME, PORT), BACKLOG);
        var newTimelineBlockNotifier = new NewTimelineBlockNotifier();
        server.createContext("/newTimelineBlock", newTimelineBlockNotifier);
        var scoreTimelineBlockHandler = new ScoreTimelineBlockHandler();
        server.createContext("/scoreTimelineBlock", scoreTimelineBlockHandler);
        var patchTimelineBlockHandler = new PatchTimelineBlockHandler();
        server.createContext("/patchTimelineBlock", patchTimelineBlockHandler);
        var updateOptaFilesHandler = new UpdateOptaFilesHandler();
        server.createContext("/updateOptaFiles", updateOptaFilesHandler);
        var consoleHandler = new ConsoleHandler();
        server.createContext("/updateConsole", consoleHandler);
        return server;
    }

    public void updateTimelineBlock(boolean print, Schedule newresult) {
        latestBestSolutions = newresult;
        synchronized (newTimelineBlockLock) {
            newTimelineBlockLock.notify();
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

    public FirebaseServer getFirebaseServer() {
        return firebaseServer;
    }

    public void setFirebaseServer(FirebaseServer firebaseServer) {
        this.firebaseServer = firebaseServer;
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

    public class ConsoleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                throw new UnsupportedOperationException();
            }

            new Thread(() -> {
                try {
                    exchange.getResponseHeaders().set(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                    exchange.sendResponseHeaders(StatusCode.CREATED.getCode(), 0);
                    OutputStream responseBody = exchange.getResponseBody();
                    responseBody.write(flushConsole().getBytes(StandardCharsets.UTF_8));
                    responseBody.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

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
                        System.out.println(updateConsole("Sending New TimelineBlock"));
                        TimelineBlock timelineBlock = new DataStructureWriter().generateTimelineBlockScore(latestBestSolutions);
                        exchange.getResponseHeaders().set(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                        exchange.sendResponseHeaders(StatusCode.CREATED.getCode(), 0);
                        OutputStream responseBody = exchange.getResponseBody();
                        responseBody.write(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(timelineBlock).getBytes(StandardCharsets.UTF_8));
                        responseBody.close();
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        }
    }

    public class ScoreTimelineBlockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                throw new UnsupportedOperationException();
            }

            new Thread(() -> {
                try {
                    byte[] response = new byte[0];
                    String javaString = URLDecoder.decode(IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8),
                            StandardCharsets.UTF_8);
                    Map<String, Map> updatedfiles = new ObjectMapper().readValue(javaString, Map.class);

                    try {
                        setFiles(updatedfiles);
                        TimelineBlock timelineBlock = problemTimelineBlock;
                        if (timelineBlock.getOrigin().equals("TCxlsb")) {
                            Schedule result = new DataStructureBuilder(valueEntryMap, timelineBlock, timeHierarchyMap)
                                    .constructChainProperty().getSchedule();
                            printCurrentSolution(result, true, "");
                            timelineBlock = new DataStructureWriter().generateTimelineBlockScore(result);
                            response = new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(timelineBlock).getBytes(StandardCharsets.UTF_8);
                        }
                    } catch (IllegalArgumentException ex) {
                        String msg = Throwables.getStackTraceAsString(ex);
                        System.err.println(updateConsole(msg));
                        response = msg.getBytes(StandardCharsets.UTF_8);
                    }

                    System.out.println(updateConsole("Sending Scored TimelineBlock"));
                    exchange.getResponseHeaders().set(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                    exchange.sendResponseHeaders(StatusCode.CREATED.getCode(), 0);
                    OutputStream responseBody = exchange.getResponseBody();
                    responseBody.write(response);
                    responseBody.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        }
    }

    public class PatchTimelineBlockHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equals("POST")) {
                throw new UnsupportedOperationException();
            }

            new Thread(() -> {
                try {
                    TimelineBlock timelineBlock = new DataStructureWriter().generateTimelineBlockScore(latestBestSolutions);
                    System.out.println(updateConsole("Sending Patched TimelineBlock"));
                    exchange.getResponseHeaders().set(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                    exchange.sendResponseHeaders(StatusCode.CREATED.getCode(), 0);
                    OutputStream responseBody = exchange.getResponseBody();
                    responseBody.write(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(timelineBlock).getBytes(StandardCharsets.UTF_8));
                    responseBody.close();
                } catch (IOException e) {
                    e.printStackTrace();
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
                    byte[] response = new byte[0];
                    String javaString = URLDecoder.decode(IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8),
                            StandardCharsets.UTF_8);
                    Map<String, Map> updatedfiles = new ObjectMapper().readValue(javaString, Map.class);

                    try {
                        setFiles(updatedfiles);
                        if (updatedfiles.containsKey("TimelineBlock.json")) {
                            if (problemTimelineBlock.getOrigin().equals("TCxlsb")) {
                                solverThread.restartSolversWithNewTimelineBlock(problemTimelineBlock);
                            }

                            TimelineBlock timelineBlock = problemTimelineBlock;
                            Schedule result = new DataStructureBuilder(valueEntryMap, timelineBlock, timeHierarchyMap)
                                    .constructChainProperty().getSchedule();
                            printCurrentSolution(result, true, "");
                            response = "{\"Updated\":true}".getBytes(StandardCharsets.UTF_8);
                        }
                    } catch (IllegalArgumentException ex) {
                        String msg = Throwables.getStackTraceAsString(ex);
                        System.err.println(updateConsole(msg));
                        response = msg.getBytes(StandardCharsets.UTF_8);
                    }

                    exchange.getResponseHeaders().
                            set(Constants.CONTENT_TYPE, Constants.APPLICATION_JSON);
                    exchange.sendResponseHeaders(StatusCode.CREATED.getCode(), 0);
                    OutputStream responseBody = exchange.getResponseBody();
                    responseBody.write(response);
                    responseBody.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();


        }
    }

    private void setFiles(Map<String, Map> updatedfiles) throws IllegalArgumentException {
        for (Map.Entry<String, Map> entry : updatedfiles.entrySet()) {
            if (entry.getKey().equals("TimeHierarchyMap.json")) {
                TimeHierarchyMap tmptimeHierarchyMap = new ObjectMapper().convertValue(entry.getValue(), TimeHierarchyMap.class);
                tmptimeHierarchyMap.checkValid();
                timeHierarchyMap = tmptimeHierarchyMap;
//                    firebaseServer.fullUpload("TimeHierarchyMap", timeHierarchyMap);
                System.out.println(updateConsole("TimeHierarchyMap Updated"));
            }
            if (entry.getKey().equals("LocationHierarchyMap.json")) {
                LocationHierarchyMap tmplocationHierarchyMap = new ObjectMapper().convertValue(entry.getValue(), LocationHierarchyMap.class);
                tmplocationHierarchyMap.checkValid();
                locationHierarchyMap = tmplocationHierarchyMap;
//                    firebaseServer.fullUpload("LocationHierarchyMap", locationHierarchyMap);
                System.out.println(updateConsole("LocationHierarchyMap Updated"));

            }
            if (entry.getKey().equals("ValueHierarchyMap.json")) {
                ValueHierarchyMap tmpvalueHierarchyMap = new ObjectMapper().convertValue(entry.getValue(), ValueHierarchyMap.class);
                tmpvalueHierarchyMap.checkValid();
                valueHierarchyMap = tmpvalueHierarchyMap;
//                    firebaseServer.fullUpload("ValueHierarchyMap", valueHierarchyMap);
                System.out.println(updateConsole("ValueHierarchyMap Updated"));
            }
            if (entry.getKey().equals("ValueEntryMap.json")) {
                ValueEntryMap tmpvalueEntryMap = new ObjectMapper().convertValue(entry.getValue(), ValueEntryMap.class);
                tmpvalueEntryMap.checkValid();
                valueEntryMap = tmpvalueEntryMap;
//                    firebaseServer.fullUpload("ValueEntryMap", valueEntryMap);
                System.out.println(updateConsole("ValueEntryMap Updated"));
            }
            if (entry.getKey().equals("Timeline.json")) {
                Timeline tmptimeline = new ObjectMapper().convertValue(entry.getValue(), Timeline.class);
                tmptimeline.checkValid();
                timeline = tmptimeline;
//                    firebaseServer.fullUpload("Timeline", timeline);
                System.out.println(updateConsole("Timeline Updated"));
            }
            if (entry.getKey().equals("TimelineBlock.json")) {
                TimelineBlock timelineBlock = new ObjectMapper().convertValue(entry.getValue(), TimelineBlock.class);
                timelineBlock.checkValid();
                setProblemTimelineBlock(timelineBlock);
                System.out.println(updateConsole("TimelineBlock Updated"));
            }
        }
    }

    public class Constants {
        public static final String CONTENT_TYPE = "Content-Type";
        public static final String APPLICATION_JSON = "application/json; charset=UTF-8";
    }
}