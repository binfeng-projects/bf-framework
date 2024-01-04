//package org.bf.framework.autoconfigure.skywalking;
//
//import io.micrometer.observation.ObservationHandler;
//import io.micrometer.observation.ObservationRegistry;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.dubbo.rpc.model.ApplicationModel;
//import org.apache.skywalking.apm.toolkit.micrometer.observation.SkywalkingDefaultTracingHandler;
//import org.apache.skywalking.apm.toolkit.micrometer.observation.SkywalkingReceiverTracingHandler;
//import org.apache.skywalking.apm.toolkit.micrometer.observation.SkywalkingSenderTracingHandler;
//import org.springframework.boot.autoconfigure.AutoConfiguration;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
//import org.springframework.context.annotation.Bean;
//
//@AutoConfiguration
//@ConditionalOnClass(SkywalkingDefaultTracingHandler.class)
//@ConditionalOnMissingBean(value = SkywalkingAutoConfig.class)
//@Slf4j
//public class SkywalkingAutoConfig  {
//    @Bean
//    ApplicationModel applicationModel(ObservationRegistry observationRegistry) {
//        ApplicationModel applicationModel = ApplicationModel.defaultModel();
//        observationRegistry.observationConfig()
//                .observationHandler(new ObservationHandler.FirstMatchingCompositeObservationHandler(
//                        new SkywalkingSenderTracingHandler(), new SkywalkingReceiverTracingHandler(),
//                        new SkywalkingDefaultTracingHandler()
//                ));
//        applicationModel.getBeanFactory().registerBean(observationRegistry);
//        return applicationModel;
//    }
//}
