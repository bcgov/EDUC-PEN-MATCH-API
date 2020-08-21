package ca.bc.gov.educ.api.penmatch.aspects;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class PenMatchLogAspect {
    @Around("@annotation(PenMatchLog)")
    public Object penMatchLog(ProceedingJoinPoint joinPoint) throws Throwable {
        ObjectMapper mapper = new ObjectMapper();
        Object[] signatureArgs = joinPoint.getArgs();
        for (Object signatureArg : signatureArgs) {
            log.debug(joinPoint.getSignature() + " Input :: \n{}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(signatureArg));
        }

        Object returnObject = joinPoint.proceed();

        if(returnObject != null) {
            log.debug(joinPoint.getSignature() + " Output :: \n{}", mapper.writerWithDefaultPrettyPrinter().writeValueAsString(returnObject));
        }
        return returnObject;
    }
}