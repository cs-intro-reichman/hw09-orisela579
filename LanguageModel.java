import java.util.HashMap;
import java.util.Random;


public class LanguageModel {

    // The map of this model.
    // Maps windows to lists of charachter data objects.
    HashMap<String, List> CharDataMap;
    
    // The window length used in this model.
    int windowLength;
    
    // The random number generator used by this model. 
	private Random randomGenerator;

    /** Constructs a language model with the given window length and a given
     *  seed value. Generating texts from this model multiple times with the 
     *  same seed value will produce the same random texts. Good for debugging. */
    public LanguageModel(int windowLength, int seed) {
        this.windowLength = windowLength;
        randomGenerator = new Random(seed);
        CharDataMap = new HashMap<String, List>();
    }

    /** Constructs a language model with the given window length.
     * Generating texts from this model multiple times will produce
     * different random texts. Good for production. */
    public LanguageModel(int windowLength) {
        this.windowLength = windowLength;
        randomGenerator = new Random();
        CharDataMap = new HashMap<String, List>();
    }

    /** Builds a language model from the text in the given file (the corpus). */
	public void train(String fileName) {
		String window = "";
        In in = new In(fileName);
        for(int i = 0; i < windowLength; i++){
            window += in.readChar();
        }
        while (!in.isEmpty()) {
            char ch = in.readChar();
            List probs = CharDataMap.get(window);
            if(probs == null){
                probs = new List();
                CharDataMap.put(window,probs);
            }
            probs.update(ch);
            window += ch;
            window = window.substring(1);
        }
        for(List probs : CharDataMap.values()){
            calculateProbabilities(probs);
        }
	}

    // Computes and sets the probabilities (p and cp fields) of all the
	// characters in the given list. */
	void calculateProbabilities(List probs) {
        int counter = 0;	
        ListIterator lst = probs.listIterator(0);
        while (lst.hasNext()) {
            counter += lst.next().count;
        }	
        double cp1 = 0;
        ListIterator lst2 = probs.listIterator(0);
        while (lst2.hasNext()) {
            CharData cd = lst2.next();
            cd.p =(double)cd.count / counter;
            cp1 += cd.p;
            cd.cp = cp1;
        }        
	}

    // Returns a random character from the given probabilities list.
	char getRandomChar(List probs) {
		double r = randomGenerator.nextDouble();
        ListIterator lst = probs.listIterator(0);
        CharData current = lst.next();
        while(r > current.cp){
            current = lst.next();
        }
		return current.chr;
	}

    /**
	 * Generates a random text, based on the probabilities that were learned during training. 
	 * @param initialText - text to start with. If initialText's last substring of size numberOfLetters
	 * doesn't appear as a key in Map, we generate no text and return only the initial text. 
	 * @param numberOfLetters - the size of text to generate
	 * @return the generated text
	 */
	public String generate(String initialText, int textLength) {
		if (initialText.length() < windowLength) {
            return initialText;
        }
        String generatedText = initialText;
        String window = generatedText.substring(generatedText.length() - windowLength);
        while (generatedText.length() < textLength + initialText.length()) {
            List probs = CharDataMap.get(window);
            if(probs == null){
                break;
            }
            char next = getRandomChar(probs);
            generatedText += next;
            window = generatedText.substring(generatedText.length() - windowLength);
        }
        return generatedText;
	}

    /** Returns a string representing the map of this language model. */
	public String toString() {
		StringBuilder str = new StringBuilder();
		for (String key : CharDataMap.keySet()) {
			List keyProbs = CharDataMap.get(key);
			str.append(key + " : " + keyProbs + "\n");
		}
		return str.toString();
	}

    public static void main(String[] args) {
		int windowLength = Integer.parseInt(args[0]);
        String initialText = args[1];
        int generatedTextLength = Integer.parseInt(args[2]);
        Boolean randomGeneration = args[3].equals("random");
        String fileName = args[4];
        // Create the LanguageModel object
        LanguageModel lm;
        if (randomGeneration)
            lm = new LanguageModel(windowLength);
        else
            lm = new LanguageModel(windowLength, 20);
        // Trains the model, creating the map.
        lm.train(fileName);
        // Generates text, and prints it.
        System.out.println(lm.generate(initialText, generatedTextLength));
        }
}
