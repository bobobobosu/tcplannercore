//package bo.tc.tcplanner.domain.solver.moves;
//
//import bo.tc.tcplanner.domain.Allocation;
//import org.apache.commons.lang3.builder.EqualsBuilder;
//import org.apache.commons.lang3.builder.HashCodeBuilder;
//import org.optaplanner.core.impl.heuristic.move.Move;
//import org.optaplanner.core.impl.score.director.ScoreDirector;
//
//import java.util.Arrays;
//import java.util.Collection;
//
//public class SplitMove implements Move {
//
//    private Allocation a1;
//    private Allocation a2;
//
//    public SplitMove(Allocation a1, Allocation a2){
//        this.a1 = a1;
//        this.a2 = a2;
//    }
//
//    @Override
//    public boolean isMoveDoable(ScoreDirector scoreDirector) {
//        return a1.getJob() != a2.getJob();
//    }
//
//    @Override
//    public Move doMove(ScoreDirector scoreDirector) {
//        scoreDirector.beforeVariableChanged(a1, "progressdelta"); // before changes are made to the queen.row
//        queen.setRow(toRow);
//        scoreDirector.afterVariableChanged(a2, "progressdelta"); // after changes are made to the queen.row
//        return this;
//    }
//
//    @Override
//    public Move rebase(ScoreDirector destinationScoreDirector) {
//        return null;
//    }
//
//    @Override
//    public String getSimpleMoveTypeDescription() {
//        return null;
//    }
//
//    @Override
//    public Collection<?> getPlanningEntities() {
//        return Arrays.asList(a1, a2);
//    }
//
//    @Override
//    public Collection<?> getPlanningValues() {
//        return Arrays.asList(a1.getProgressdelta(), a2.getProgressdelta());
//    }
//
//    @Override
//    public int hashCode() {
//        return new HashCodeBuilder()
//                .append(a1)
//                .append(toRow)
//                .toHashCode();
//    }
//
//    @Override
//    public boolean equals(Object o) {
//        if (this == o) {
//            return true;
//        } else if (o instanceof SplitMove) {
//            SplitMove other = (SplitMove) o;
//            return new EqualsBuilder()
//                    .append(a1, other.a1)
//                    .append(a2, other.a2)
//                    .isEquals();
//        } else {
//            return false;
//        }
//    }
//}
