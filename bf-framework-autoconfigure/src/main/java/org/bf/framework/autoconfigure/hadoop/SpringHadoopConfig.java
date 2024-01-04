package org.bf.framework.autoconfigure.hadoop;

import org.springframework.data.hadoop.config.annotation.SpringHadoopConfigurerAdapter;
import org.springframework.data.hadoop.config.annotation.builders.HadoopConfigConfigurer;

public class SpringHadoopConfig extends SpringHadoopConfigurerAdapter {
    private HadoopProperties shp;

    public SpringHadoopConfig(HadoopProperties shp) {
        this.shp = shp;
    }

    @Override
    public void configure(HadoopConfigConfigurer config) throws Exception {
        config
                .fileSystemUri(shp.getFsUri())
                .resourceManagerAddress(shp.getResourceManagerAddress())
                .jobHistoryAddress(shp.getJobHistoryAddress())
                .withProperties()
                .properties(shp.getConfig())
                .and()
                .withResources()
                .resources(shp.getResources())
                .and()
                .withSecurity()
                .namenodePrincipal(shp.getSecurity() != null ? shp.getSecurity().getNamenodePrincipal() : null)
                .rmManagerPrincipal(shp.getSecurity() != null ? shp.getSecurity().getRmManagerPrincipal() : null)
                .authMethod(shp.getSecurity() != null ? shp.getSecurity().getAuthMethod() : null)
                .userPrincipal(shp.getSecurity() != null ? shp.getSecurity().getUserPrincipal() : null)
                .userKeytab(shp.getSecurity() != null ? shp.getSecurity().getUserKeytab() : null);
    }

}