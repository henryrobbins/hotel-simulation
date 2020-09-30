package com.henryrobbins;

import java.nio.file.Path;
import java.nio.file.Paths;

/** Maintains a path to resources */
public abstract class ResourcesPath {

	/** Path to the resources */
	private static Path path= Paths.get("src", "resources");

	/** Set the path */
	public static void setPath(Path path) {
		ResourcesPath.path= path;
	}

	/** Get the path */
	public static Path path() {
		return path;
	}
}