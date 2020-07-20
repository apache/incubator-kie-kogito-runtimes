package org.drools.core.common;

import java.util.Map;

import org.drools.core.impl.InternalKnowledgeBase;
import org.drools.core.phreak.RuleAgendaItem;
import org.drools.core.phreak.RuleExecutor;
import org.drools.core.reteoo.RuleTerminalNodeLeftTuple;
import org.drools.core.rule.Declaration;
import org.drools.core.spi.Activation;
import org.drools.core.spi.RuleFlowGroup;
import org.drools.core.util.index.TupleList;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.Match;

public class KogitoDefaultAgenda extends DefaultAgenda implements KogitoInternalAgenda {

    public KogitoDefaultAgenda(InternalKnowledgeBase kBase) {
        super(kBase);
    }

    public KogitoDefaultAgenda(InternalKnowledgeBase kBase, boolean initMain) {
        super(kBase, initMain);
    }

    @Override
    public boolean isRuleActiveInRuleFlowGroup(String ruleflowGroupName, String ruleName, String processInstanceId) {
        return isRuleInstanceAgendaItem(ruleflowGroupName, ruleName, processInstanceId);
    }

    @Override
    public void activateRuleFlowGroup(String name, String processInstanceId, String nodeInstanceId) {

    }

    @Override
    public boolean isRuleInstanceAgendaItem(String ruleflowGroupName, String ruleName, String processInstanceId) {
        propagationList.flush();
        RuleFlowGroup systemRuleFlowGroup = this.getRuleFlowGroup(ruleflowGroupName );

        Match[] matches = ((InternalAgendaGroup)systemRuleFlowGroup).getActivations();
        for ( Match match : matches ) {
            Activation act = ( Activation ) match;
            if ( act.isRuleAgendaItem() ) {
                // The lazy RuleAgendaItem must be fully evaluated, to see if there is a rule match
                RuleExecutor ruleExecutor = ((RuleAgendaItem) act).getRuleExecutor();
                ruleExecutor.evaluateNetwork(this);
                TupleList list = ruleExecutor.getLeftTupleList();
                for (RuleTerminalNodeLeftTuple lt = (RuleTerminalNodeLeftTuple) list.getFirst(); lt != null; lt = (RuleTerminalNodeLeftTuple) lt.getNext()) {
                    if ( ruleName.equals( lt.getRule().getName() )
                            && ( checkProcessInstance( lt, processInstanceId ) )) {
                        return true;
                    }
                }

            }   else {
                if ( ruleName.equals( act.getRule().getName() )
                        && ( checkProcessInstance( act, processInstanceId ) )) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkProcessInstance(Activation activation,
                                         String processInstanceId) {
        final Map<String, Declaration> declarations = activation.getSubRule().getOuterDeclarations();
        for ( Declaration declaration : declarations.values() ) {
            if ( "processInstance".equals( declaration.getIdentifier() )
                    || "org.kie.api.runtime.process.WorkflowProcessInstance".equals(declaration.getTypeName())) {
                Object value = declaration.getValue( workingMemory,
                                                     activation.getTuple().get( declaration ).getObject() );
                if ( value instanceof ProcessInstance) {
                    return ((ProcessInstance) value).getId().equals(processInstanceId);
                }
            }
        }
        return true;
    }
}
