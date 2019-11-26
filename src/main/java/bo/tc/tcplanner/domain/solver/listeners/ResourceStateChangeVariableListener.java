//package bo.tc.tcplanner.domain.solver.listeners;
//
//import bo.tc.tcplanner.datastructure.ResourceElement;
//import bo.tc.tcplanner.domain.Allocation;
//import bo.tc.tcplanner.domain.Schedule;
//import org.kie.api.definition.rule.All;
//import org.kie.internal.builder.ResourceChange;
//import org.optaplanner.core.impl.domain.variable.listener.VariableListener;
//import org.optaplanner.core.impl.score.director.ScoreDirector;
//
//import java.util.*;
//
//import static bo.tc.tcplanner.datastructure.converters.DataStructureBuilder.dummyJob;
//import static bo.tc.tcplanner.domain.solver.listeners.ListenerTools.updateAllocationPreviousStandstill;
//
//public class ResourceStateChangeVariableListener implements VariableListener<Allocation> {
//    @Override
//    public void beforeEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {
//
//    }
//
//    @Override
//    public void afterEntityAdded(ScoreDirector scoreDirector, Allocation allocation) {
//        updateAllocation(scoreDirector,allocation);
//    }
//
//    @Override
//    public void beforeVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
//
//    }
//
//    @Override
//    public void afterVariableChanged(ScoreDirector scoreDirector, Allocation allocation) {
//        updateAllocation(scoreDirector,allocation);
//    }
//
//    @Override
//    public void beforeEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {
//
//    }
//
//    @Override
//    public void afterEntityRemoved(ScoreDirector scoreDirector, Allocation allocation) {
//        updateAllocation(scoreDirector,allocation);
//    }
//
//    protected void updateAllocation(ScoreDirector scoreDirector, Allocation originalAllocation){
//        Schedule schedule = (Schedule) scoreDirector.getWorkingSolution();
//        Queue<Allocation> uncheckedSuccessorQueue = new ArrayDeque<>();
//        uncheckedSuccessorQueue.add(originalAllocation);
//        while (!uncheckedSuccessorQueue.isEmpty()) {
//            Allocation allocation = uncheckedSuccessorQueue.remove();
//            boolean updated = update(scoreDirector, allocation);
//            if (updated) {
//                uncheckedSuccessorQueue.addAll(allocation.getSuccessorAllocationList());
//            }
//        }
//
//        int i=0;
//    }
//
//    protected boolean update(ScoreDirector scoreDirector, Allocation allocation) {
//        scoreDirector.beforeVariableChanged(allocation, "resourceStateChange_absolute");
//        updateAllocationResourceStateChange(allocation);
//        scoreDirector.afterVariableChanged(allocation, "resourceStateChange_absolute");
//
//        if(allocation.getJob().getName() == "洗澡"){
////            System.out.println(allocation.getJob().getName() + " " + allocation.getJob().getRownum());
//        }
//
//        return true;
//    }
//}
