package org.bf.framework.autoconfigure.hive.support;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

public class HiveRunnerTasklet implements Callable<List<String>>,Tasklet {

	private HiveTemplate hiveTemplate;
	//--------------common------------------
	private Collection<HiveScript> scripts;
	public void setScripts(Collection<HiveScript> scripts) {
		this.scripts = scripts;
	}
	protected List<String> executeHiveScripts() {
		if (CollectionUtils.isEmpty(scripts)) {
			return Collections.emptyList();
		}
		return hiveTemplate.executeScript(scripts);
	}

	public void setHiveTemplate(HiveTemplate hiveTemplate) {
		this.hiveTemplate = hiveTemplate;
	}
	//--------------runner------------------
	private Iterable<Callable<?>> preActions;
	private Iterable<Callable<?>> postActions;

	@Override
	public List<String> call() throws Exception {
		call(preActions);
		List<String> result = executeHiveScripts();
		call(postActions);
		return result;
	}

	public void setPreAction(Collection<Callable<?>> actions) {
		this.preActions = actions;
	}

	public void setPostAction(Collection<Callable<?>> actions) {
		this.postActions = actions;
	}

	private void call(Iterable<Callable<?>> actions) throws Exception {
		if (actions != null) {
			for (Callable<?> action : actions) {
				action.call();
			}
		}
	}
	//--------------tasklet------------------
	@Override
	public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
		executeHiveScripts();
		return RepeatStatus.FINISHED;
	}
}