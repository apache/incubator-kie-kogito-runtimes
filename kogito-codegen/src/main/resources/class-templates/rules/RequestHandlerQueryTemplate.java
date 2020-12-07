package com.myspace.demo;

import java.util.List;

import javax.inject.Named;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.kie.kogito.rules.RuleUnit;
import org.kie.kogito.rules.RuleUnitInstance;

@Named("$endpointName$")
public class $unit$Query$name$RequestHandler implements RequestHandler<$UnitType$, $ReturnType$> {

    RuleUnit<$UnitType$> ruleUnit;

    public $unit$Query$name$RequestHandler() {
    }

    public $unit$Query$name$RequestHandler(RuleUnit<$UnitType$> ruleUnit) {
        this.ruleUnit = ruleUnit;
    }

    /**
     * Returns same thing as /first
     *
     * @param unitDTO
     * @return
     */
    @Override
    public $ReturnType$ handleRequest($UnitTypeDTO$ unitDTO, Context context) {
        RuleUnitInstance<$UnitType$> instance = ruleUnit.createInstance();
        List<$ReturnType$> results = instance.executeQuery($unit$Query$name$.class);
        $ReturnType$ response = results.isEmpty() ? null : results.get(0);
        return response;
    }
}
