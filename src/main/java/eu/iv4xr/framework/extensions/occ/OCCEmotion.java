package eu.iv4xr.framework.extensions.occ;

import eu.iv4xr.framework.mainConcepts.IEmotion;

/**
 * A wrapper over OCC's Emotion object that implements the interface
 * {@link IEmotion}. An OCCEmotion object is essentially a tuple
 * (g,type,i,t0,...) where type is the type of the emotion,
 * e.g. fear, and i is its intensity, and t0 is the time when it is
 * stimulated. g is the target of the emotion, in the current setup
 * this represents some goal e.g. "finishing a game".
 * 
 * @author Wish  
 */
public class OCCEmotion implements IEmotion {
	
	/**
	 * The goal, toward which the emotion is directed.
	 */
	public String goalName ;
	public Emotion em ;
	
	public OCCEmotion(String goalName, Emotion em) {
		this.goalName = goalName ;
		this.em = em ;
	}

	@Override
	public String getEmotionType() {
		return em.etype.toString() ;
	}

	@Override
	public String getAgentId() {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public String getTargetId() {
		throw new UnsupportedOperationException() ;
	}

	@Override
	public float getIntensity() {
		return em.intensity ;
	}

	@Override
	public Long getTime() {
		return null ;
	}

	@Override
	public Long getActivationTime() {
		return (long) em.t0 ;
	}

}
