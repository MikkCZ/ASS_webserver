package ass.stankmic.server.requests;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WordCounter {
	
	private final Set<String> words;
	
	private WordCounter() {
		this.words = Collections.newSetFromMap(new ConcurrentHashMap<>(1_000_000));
	}
	
	public void addWords(String text) {
		final String[] wordsInText = text.split("\\s+");
		for(String word : wordsInText) {
			this.words.add(word);
		}
	}
	
	public synchronized int getCount() {
		final int count = words.size();
		words.clear();
		return count;
	}
	
	private static final WordCounter instance;
	
	static {
		instance = new WordCounter();
	}
	
	public static WordCounter getInstance() {
		return instance;
	}
}
