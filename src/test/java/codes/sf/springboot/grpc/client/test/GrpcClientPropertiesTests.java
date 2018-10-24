package codes.sf.springboot.grpc.client.test;

import com.openpojo.reflection.filters.FilterClassName;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;
import org.junit.Test;

public class GrpcClientPropertiesTests {

    @Test
    public void propertiesTest() {

        Validator validator = ValidatorBuilder.create()
                .with(new GetterMustExistRule())
                .with(new SetterMustExistRule())
                .with(new SetterTester())
                .with(new GetterTester())
                .build();

        validator.validate("codes.sf.springboot.grpc.client.autoconfigure",
                new FilterClassName("GrpcClientProperties"));
    }

}
