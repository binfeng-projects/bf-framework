/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bf.framework.autoconfigure.yarn.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.yarn.boot.properties.AbstractResourceProperties;

@ConfigurationProperties(value = "spring.yarn.appmaster.resource")
public class SpringYarnAppmasterResourceProperties extends AbstractResourceProperties {

	private Integer priority;
	private String labelExpression;

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public String getLabelExpression() {
		return labelExpression;
	}

	public void setLabelExpression(String labelExpression) {
		this.labelExpression = labelExpression;
	}

}
