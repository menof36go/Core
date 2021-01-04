package edu.kit.ipd.consistency_analyzer.textproviders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import edu.kit.ipd.consistency_analyzer.datastructures.DependencyTag;
import edu.kit.ipd.consistency_analyzer.datastructures.IText;
import edu.kit.ipd.consistency_analyzer.datastructures.IWord;
import edu.kit.ipd.consistency_analyzer.datastructures.PosTag;
import edu.kit.ipd.parse.luna.graph.IArc;
import edu.kit.ipd.parse.luna.graph.IGraph;
import edu.kit.ipd.parse.luna.graph.INode;

public class ParseConverter {

	private IText annotatedText;
	private IGraph graph;

	private Map<INode, Word> instances;
	private List<Word> orderedWords;

	public ParseConverter(IGraph graph) {
		this.graph = graph;
	}

	public void convert() {
		this.reset();

		this.createWords();
		this.createDeps();

		this.createText();
	}

	public IText getAnnotatedText() {
		return annotatedText;
	}

	private void createWords() {
		instances = new HashMap<>();
		orderedWords = new ArrayList<>();

		List<INode> tokens = this.graph.getNodesOfType(this.graph.getNodeType("token"));

		for (INode token : tokens) {
			Word word = new Word(token);
			this.orderedWords.add(word);
			this.instances.put(token, word);
		}
	}

	private void createDeps() {
		// TODO: To Complete

		Map<String, DependencyTag> dependencyMap = Arrays.stream(DependencyTag.values()).collect(Collectors.toMap(d -> String.valueOf(d).toLowerCase(), d -> d));

		for (INode node : graph.getNodesOfType(this.graph.getNodeType("token"))) {
			Word sourceWord = this.instances.get(node);
			for (IArc arc : node.getOutgoingArcsOfType(graph.getArcType("typedDependency"))) {
				Word targetWord = instances.get(arc.getTargetNode());

				String arcAttributeValue = String.valueOf(arc.getAttributeValue("relationShort"));
				if (dependencyMap.containsKey(arcAttributeValue)) {
					DependencyTag depTag = dependencyMap.get(arcAttributeValue);
					sourceWord.wordsThatAreDependenciesOfThis.get(depTag).add(targetWord);
					targetWord.wordsThatAreDependentOnThis.get(depTag).add(sourceWord);
				}
			}
		}

	}

	private void createText() {
		IText text = new Text(this.orderedWords);
		this.annotatedText = text;
	}

	private void reset() {
		this.annotatedText = null;
		this.instances = null;
		this.orderedWords = null;
	}

	private static final class Word implements IWord {
		private final int sentence;
		private final int position;
		private final String text;
		private final PosTag posTag;
		private final String lemma;

		private final Map<DependencyTag, List<IWord>> wordsThatAreDependenciesOfThis = Arrays.stream(DependencyTag.values()).collect(Collectors.toMap(t -> t, v -> new ArrayList<>()));
		private final Map<DependencyTag, List<IWord>> wordsThatAreDependentOnThis = Arrays.stream(DependencyTag.values()).collect(Collectors.toMap(t -> t, v -> new ArrayList<>()));

		private transient IText parent;

		Word(INode node) {
			text = String.valueOf(node.getAttributeValue("value"));
			position = Integer.valueOf(String.valueOf(node.getAttributeValue("position")));
			lemma = String.valueOf(node.getAttributeValue("lemma"));
			posTag = getPosTag(node);
			sentence = Integer.valueOf(String.valueOf(node.getAttributeValue("sentenceNumber")));
		}

		private PosTag getPosTag(INode node) {

			Map<String, PosTag> posTagMap = Arrays.stream(PosTag.values()).collect(Collectors.toMap(d -> String.valueOf(d), d -> d));

			return posTagMap.get(String.valueOf(node.getAttributeValue("pos")));

		}

		@Override
		public int getSentenceNo() {
			return sentence;
		}

		@Override
		public String getText() {
			return text;
		}

		@Override
		public PosTag getPosTag() {
			return posTag;
		}

		@Override
		public int getPosition() {
			return position;
		}

		@Override
		public String getLemma() {
			return this.lemma;
		}

		@Override
		public List<IWord> getWordsThatAreDependencyOfThis(DependencyTag dependencyTag) {
			return wordsThatAreDependenciesOfThis.get(dependencyTag);
		}

		@Override
		public List<IWord> getWordsThatAreDependentOnThis(DependencyTag dependencyTag) {
			return wordsThatAreDependentOnThis.get(dependencyTag);
		}

		@Override
		public IWord getPreWord() {
			if (position == 0) {
				return null;
			}
			return parent.getNodes().get(position - 1);
		}

		@Override
		public IWord getNextWord() {
			if (position == parent.getLength() - 1) {
				return null;
			}
			return parent.getNodes().get(position + 1);
		}
	}

	private static final class Text implements IText {

		private List<IWord> words;

		public Text(List<Word> orderedWords) {
			orderedWords.stream().forEach(w -> w.parent = this);
			this.words = Collections.unmodifiableList(orderedWords);

		}

		@Override
		public IWord getStartNode() {
			return this.words.isEmpty() ? null : this.words.get(0);
		}

		@Override
		public List<IWord> getNodes() {
			return this.words;
		}
	}
}