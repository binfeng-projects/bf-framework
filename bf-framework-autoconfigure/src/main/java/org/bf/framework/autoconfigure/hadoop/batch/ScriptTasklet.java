/*
 * Copyright 2011-2013 the original author or authors.
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
package org.bf.framework.autoconfigure.hadoop.batch;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.batch.core.step.tasklet.CallableTaskletAdapter;

import java.util.concurrent.Callable;

/**
 * 可以用 {@link CallableTaskletAdapter}
 */
@Deprecated
public class ScriptTasklet implements Tasklet {

	private Callable<Object> scriptCallback;

	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		scriptCallback.call();
		return RepeatStatus.FINISHED;
	}

	/**
	 * @param scriptCallback Callback to the underlying script.
	 */
	public void setScriptCallback(Callable<Object> scriptCallback) {
		this.scriptCallback = scriptCallback;
	}
}
