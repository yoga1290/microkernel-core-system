package yoga1290.coresystem.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import yoga1290.coresystem.services.IService;

import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Aspect
@Component
public class AOPLoggingConfig {

    public static final String TRANSACTION_ID = "TRANSACTION_ID";
    public static final String URI = "URI";

    // https://stackoverflow.com/a/40679829
    @Pointcut("@target(controllerClazz) && @annotation(requestMapping) && execution(* *(..))")
    public void controller(Controller controllerClazz, RequestMapping requestMapping) {}

    @Around("controller(controllerClazz, requestMapping)")
    public Object logControllers(
                            ProceedingJoinPoint joinPoint,
                            Controller controllerClazz,
                            RequestMapping requestMapping) throws Throwable {
        String uri = String.format("%s", Arrays.asList(requestMapping.value()));
        MDC.put(TRANSACTION_ID, UUID.randomUUID().toString());
        MDC.put(URI, uri);

        Object ret = null;
        try {
            ret = logAround(joinPoint);
        } catch(Exception e) {
//            MDC.clear();
            e.printStackTrace(); //TODO: proper logging
            throw e;
        }
        MDC.clear();
        return ret;
    }

    @Pointcut("target(serviceClass) && execution(* *(..))")
    public void service(IService serviceClass) {}
    @Around("service(serviceClass)")
    public Object logServices(
            ProceedingJoinPoint joinPoint,
            IService serviceClass) throws Throwable {
        return logAround(joinPoint);
    }

    private Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        Object ret = null;
        String logStr = "";
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodStr = formatMethod(signature, joinPoint.getArgs());
        long stime = System.currentTimeMillis();

        try{
            ret = joinPoint.proceed(joinPoint.getArgs());

            logStr = String.format("%s | end time: %s | duration: %s",
                    methodStr,
                    System.currentTimeMillis(),
                    System.currentTimeMillis() - stime);
            log.info(logStr);
        } catch (Throwable e) {

            logStr = String.format("%s | end time: %s | duration: %s | Exception: %s",
                    methodStr,
                    System.currentTimeMillis(),
                    System.currentTimeMillis() - stime,
                    e.getMessage());
            log.error(logStr);
            throw e;
        }
        return ret;
    }
    private String formatMethod(MethodSignature signature, Object[] args) {
        String classFullName = signature.getDeclaringTypeName();
        String methodName = signature.getMethod().getName();
        String argStr = Arrays.asList(args).toString();
        boolean invalid = classFullName == null;
        if (invalid) {
            return "";
        }
        int startIndexOfClassName = classFullName.lastIndexOf(".") + 1;
        classFullName = classFullName.substring(startIndexOfClassName);

        return String.format("%s.%s(%s)", classFullName, methodName, argStr);
    }
}
