package org.bf.framework.autoconfigure.yarn.properties;

import java.util.List;
import java.util.Map;

public abstract class AbstractLaunchContextProperties {

	/**
	 * 主命令。例如/usr/jdk/bin/java
	 */
	private String command;
	private String archiveFile;

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	private String runnerClass;
	private List<String> options;
	private Map<String, String> arguments;
	private List<String> argumentsList;
	private List<String> containerAppClasspath;
	private String pathSeparator;
	private boolean includeBaseDirectory = true;
	private boolean useYarnAppClasspath = false;
	private boolean useMapreduceAppClasspath = false;
	private boolean includeLocalSystemEnv = false;

	public String getArchiveFile() {
		return archiveFile;
	}

	public void setArchiveFile(String archiveFile) {
		this.archiveFile = archiveFile;
	}

	public String getRunnerClass() {
		return runnerClass;
	}

	public void setRunnerClass(String runnerClass) {
		this.runnerClass = runnerClass;
	}

	public List<String> getOptions() {
		return options;
	}

	public void setOptions(List<String> options) {
		this.options = options;
	}

	public Map<String, String> getArguments() {
		return arguments;
	}

	public void setArguments(Map<String, String> arguments) {
		this.arguments = arguments;
	}

	public List<String> getContainerAppClasspath() {
		return containerAppClasspath;
	}

	public List<String> getArgumentsList() {
		return argumentsList;
	}

	public void setArgumentsList(List<String> argumentsList) {
		this.argumentsList = argumentsList;
	}

	public void setContainerAppClasspath(List<String> containerAppClasspath) {
		this.containerAppClasspath = containerAppClasspath;
	}

	public String getPathSeparator() {
		return pathSeparator;
	}

	public void setPathSeparator(String pathSeparator) {
		this.pathSeparator = pathSeparator;
	}

	public boolean isIncludeBaseDirectory() {
		return includeBaseDirectory;
	}

	public void setIncludeBaseDirectory(boolean includeBaseDirectory) {
		this.includeBaseDirectory = includeBaseDirectory;
	}

	public boolean isUseYarnAppClasspath() {
		return useYarnAppClasspath;
	}

	public void setUseYarnAppClasspath(boolean useYarnAppClasspath) {
		this.useYarnAppClasspath = useYarnAppClasspath;
	}

	public boolean isUseMapreduceAppClasspath() {
		return useMapreduceAppClasspath;
	}

	public void setUseMapreduceAppClasspath(boolean useMapreduceAppClasspath) {
		this.useMapreduceAppClasspath = useMapreduceAppClasspath;
	}

	public boolean isIncludeLocalSystemEnv() {
		return includeLocalSystemEnv;
	}

	public void setIncludeLocalSystemEnv(boolean includeLocalSystemEnv) {
		this.includeLocalSystemEnv = includeLocalSystemEnv;
	}

}
