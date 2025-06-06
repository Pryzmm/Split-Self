package com.pryzmm.splitself;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

public class SplitSelf implements ModInitializer {
	public static final String MOD_ID = "splitself";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Hello, ", System.getProperty("user.name"));
		String[] logInitList = {"You recognize us, don't you?", "We're here to observe.", "Free from parallelism."};
		LOGGER.info(logInitList[(new Random()).nextInt(logInitList.length)]);
	}
}