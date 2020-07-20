package org.drools.core.common;

import org.drools.core.impl.InternalKnowledgeBase;

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
        InternalRuleFlowGroup ruleFlowGroup = (InternalRuleFlowGroup) getRuleFlowGroup( name );
        activateRuleFlowGroup( ruleFlowGroup, processInstanceId, nodeInstanceId );
    }

    @Override
    public boolean isRuleInstanceAgendaItem(String ruleflowGroupName, String ruleName, String processInstanceId) {
        return isRuleInstanceAgendaItem(ruleflowGroupName, ruleName, (Object) processInstanceId);
    }
}
