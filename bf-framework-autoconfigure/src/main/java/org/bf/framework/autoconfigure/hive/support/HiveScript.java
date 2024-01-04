package org.bf.framework.autoconfigure.hive.support;

import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Holder class for a Hive script.
 *
 * @author Costin Leau
 */
public class HiveScript {

	private Resource resource;
	private Map<String, String> arguments;

	/**
	 * Constructs a new <code>HiveScript</code> instance from the given
	 * resource.
	 *
	 * @param resource script resource.
	 */
	public HiveScript(Resource resource) {
		this(resource, (Map<String, String>) null);
	}

	public HiveScript(Resource resource, Map<?, ?> args) {
		Assert.notNull(resource, "a valid resource is required");
		this.resource = resource;
		if (args != null) {
			arguments = new LinkedHashMap<String, String>();
			for (Map.Entry<?, ?> entry : args.entrySet()) {
				arguments.put(entry.getKey().toString(), entry.getValue().toString());
			}
		}
	}

	/**
	 * Constructs a new <code>HiveScript</code> instance. Both the script
	 * content and its parameters are supplied.
	 *
	 * @param resource script content.
	 * @param args script arguments.
	 */
	public HiveScript(Resource resource, Properties args) {
		Assert.notNull(resource, "a valid resource is required");
		this.resource = resource;
		if (args != null) {
			Set<String> props = args.stringPropertyNames();
			arguments = new LinkedHashMap<String, String>();
			for (String prop : props) {
				arguments.put(prop, args.getProperty(prop));
			}
		}
	}

	/**
	 * Returns the script resource.
	 *
	 * @return Returns the resource
	 */
	public Resource getResource() {
		return resource;
	}

	/**
	 * Returns the arguments associated with this script.
	 *
	 * @return Returns the arguments for this script.
	 */
	public Map<String, String> getArguments() {
		return arguments;
	}

	@Override
	public String toString() {
		return resource.getDescription();
	}
}