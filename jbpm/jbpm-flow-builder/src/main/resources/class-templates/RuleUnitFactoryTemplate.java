class Template {
    Object f = new org.jbpm.workflow.core.node.RuleUnitFactory<$Type$>() {
        public $Type$ bind(org.kie.kogito.internal.runtime.process.ProcessContext kcontext) {
            return null;
        }
        public org.kie.kogito.rules.RuleUnit<$Type$> unit() {
            return null;
        }
        public void unbind(org.kie.kogito.internal.runtime.process.ProcessContext kcontext, $Type$ unit) {

        }
    };
}
