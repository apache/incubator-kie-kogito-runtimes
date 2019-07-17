package com.myspace.demo;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.kie.kogito.rules.RuleUnit;
import org.kie.kogito.rules.RuleUnitInstance;

@Path("/$name$")
public class $unit$Query$name$Endpoint {

    RuleUnit<$UnitType$> ruleUnit;

    public $unit$Query$name$Endpoint() { }

    public $unit$Query$name$Endpoint(RuleUnit<$UnitType$> ruleUnit) {
        this.ruleUnit = ruleUnit;
    }

    @POST()
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> executeQuery($UnitType$ unit) {
        RuleUnitInstance<$UnitType$> instance = ruleUnit.createInstance(unit);
        return instance.executeQuery( "$queryName$" );
    }
}
