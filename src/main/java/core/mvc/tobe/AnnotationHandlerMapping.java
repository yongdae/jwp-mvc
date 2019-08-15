package core.mvc.tobe;

import com.google.common.collect.Maps;
import core.annotation.web.RequestMapping;
import core.annotation.web.RequestMethod;
import core.mvc.HandlerMapping;
import core.mvc.tobe.scanner.ComponentScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

/**
 * @author : yusik
 * @date : 2019-08-15
 */
public class AnnotationHandlerMapping implements HandlerMapping {

    private static final Logger logger = LoggerFactory.getLogger(AnnotationHandlerMapping.class);
    private String[] basePackages;
    private Map<HandlerKey, HandlerExecution> handlerExecutions = Maps.newHashMap();
    private Map<String, HandlerKey> urlMappings = Maps.newHashMap();

    public AnnotationHandlerMapping(String... basePackages) {
        this.basePackages = basePackages;
    }

    @Override
    public HandlerMapping initialize() {
        Map<Class<?>, Object> beans = ComponentScanner.getControllers(basePackages);
        for (Class controller : beans.keySet()) {
            registerHandler(beans.get(controller), controller.getDeclaredMethods());
        }

        return this;
    }

    private void registerHandler(Object bean, Method[] methods) {
        for (Method method : methods) {
            RequestMapping mapping = method.getDeclaredAnnotation(RequestMapping.class);
            RequestMethod[] requestMethod = mapping.method();
            String[] urls = mapping.value();
            HandlerKey handlerKey = new HandlerKey(urls, requestMethod);

            Arrays.stream(urls).forEach(url -> urlMappings.put(url, handlerKey));
            handlerExecutions.put(handlerKey, new HandlerExecution(bean, method));
            logger.info("key: {}, handler: {}", handlerKey, handlerExecutions.get(handlerKey));
        }
    }

    @Override
    public HandlerExecution getHandler(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        HandlerKey handlerKey = this.urlMappings.get(requestUri);
        return handlerExecutions.get(handlerKey);
    }
}
