package local.radioschedulers.alg.ga.jgap;

import gnu.trove.THashSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.jgap.BaseGene;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.ICompareToHandler;
import org.jgap.IPersistentRepresentation;
import org.jgap.InvalidConfigurationException;
import org.jgap.RandomGenerator;
import org.jgap.UnsupportedRepresentationException;

public class BetterSetGene<V> extends BaseGene implements
		IPersistentRepresentation {

	private static final long serialVersionUID = 1L;

	protected THashSet<V> m_geneSet = new THashSet<V>();

	private V m_value;

	/**
	 * Default constructor.
	 * <p>
	 * Attention: The configuration used is the one set with the static method
	 * Genotype.setConfiguration.
	 * 
	 * @throws InvalidConfigurationException
	 */
	public BetterSetGene() throws InvalidConfigurationException {
		this(Genotype.getStaticConfiguration());
	}

	/**
	 * @param a_conf
	 *            the configuration to use
	 * 
	 * @throws InvalidConfigurationException
	 * 
	 * @author Klaus Meffert
	 * @since 3.0
	 */
	public BetterSetGene(final Configuration a_conf)
			throws InvalidConfigurationException {
		super(a_conf);
	}

	protected Gene newGeneInternal() {
		try {
			BetterSetGene<V> sg = new BetterSetGene<V>(getConfiguration());
			sg.addAlleles(getAlleles());
			sg.setAllele(getAllele());
			return sg;
		} catch (InvalidConfigurationException iex) {
			throw new IllegalStateException(iex.getMessage());
		}
	}

	/**
	 * Adds a potential allele value to the collection.
	 * 
	 * @param a_value
	 *            the Integer value to be added
	 */
	public void addAllele(final V a_value) {
		m_geneSet.add(a_value);
	}

	/**
	 * Add a set of potential allele values to the collection
	 * 
	 * @param a_alleles
	 *            the set of alleles to be added
	 */
	public void addAlleles(final Collection<V> a_alleles) {
		m_geneSet.addAll(a_alleles);
	}

	/**
	 * Removes a potential allele or set of alleles from the collection.
	 * 
	 * @param a_key
	 *            the unique value(s) of the object(s) to be removed
	 */
	public void removeAllele(final V a_key) {
		m_geneSet.remove(a_key);
	}

	public THashSet<V> getAlleles() {
		return m_geneSet;
	}

	/**
	 * Sets the allele value to be a random value using a defined random number
	 * generator.
	 * 
	 * @author Johnathan Kool
	 * 
	 * @param a_numberGenerator
	 *            RandomGenerator
	 */
	@SuppressWarnings("unchecked")
	public void setToRandomValue(final RandomGenerator a_numberGenerator) {
		m_value = (V) m_geneSet.toArray()[a_numberGenerator.nextInt(m_geneSet
				.size())];
	}

	/**
	 * See interface Gene for description of applyMutation.
	 * 
	 * For this kind of gene, providing an index and a magnitude have no
	 * significance because the individual allele forms are independent of one
	 * another. In mutating, they can only cange from one form to another. It
	 * may be possible to weight the likelihood of mutation to different forms,
	 * but that will not be implemented here.
	 * 
	 * @param a_index
	 *            ignored here
	 * @param a_percentage
	 *            ignored here
	 * 
	 * @author Klaus Meffert
	 * @author Johnathan Kool
	 * @since 2.4
	 */
	public void applyMutation(final int a_index, final double a_percentage) {
		RandomGenerator rn;
		if (getConfiguration() != null) {
			rn = getConfiguration().getRandomGenerator();
		} else {
			rn = getConfiguration().getJGAPFactory().createRandomGenerator();
		}
		setToRandomValue(rn);
	}

	/**
	 * Sets the value and internal state of this Gene from the string
	 * representation returned by a previous invocation of the
	 * getPersistentRepresentation() method. This is an optional method but, if
	 * not implemented, XML persistence and possibly other features will not be
	 * available. An UnsupportedOperationException should be thrown if no
	 * implementation is provided.
	 * 
	 * @param a_representation
	 *            the string representation retrieved from a prior call to the
	 *            getPersistentRepresentation() method
	 * @throws UnsupportedRepresentationException
	 *             if this Gene implementation does not support the given string
	 *             representation
	 * 
	 * @author Neil Rostan
	 * @since 1.0
	 */
	@SuppressWarnings("unchecked")
	public void setValueFromPersistentRepresentation(String a_representation)
			throws UnsupportedRepresentationException {
		if (a_representation != null) {
			StringTokenizer tokenizer = new StringTokenizer(a_representation,
					PERSISTENT_FIELD_DELIMITER);
			// Make sure the representation contains the correct number of
			// fields. If not, throw an exception.
			// -----------------------------------------------------------
			if (tokenizer.countTokens() < 3) {
				throw new UnsupportedRepresentationException(
						"The format of the given persistent representation "
								+ "is not recognized: it must contain at least three tokens.");
			}
			String valueRepresentation = tokenizer.nextToken();
			// First parse and set the representation of the value.

			// ----------------------------------------------------
			if (valueRepresentation.equals("null")) {
				m_value = null;
			} else {
				try {
					m_value = (V) new Integer(Integer
							.parseInt(valueRepresentation));
				} catch (NumberFormatException e) {
					throw new UnsupportedRepresentationException(
							"The format of the given persistent representation "
									+ "is not recognized: field 1 does not appear to be "
									+ "an integer value.");
				}
			}
			// Parse the potential categories.
			// -------------------------------
			V allele;
			while (tokenizer.hasMoreTokens()) {
				try {
					allele = (V) new Integer(Integer.parseInt(tokenizer
							.nextToken()));
					m_geneSet.add(allele);
				} catch (NumberFormatException e) {
					throw new UnsupportedRepresentationException(
							"The format of the given persistent representation "
									+ "is not recognized: a member of the list of eligible values "
									+ "does not appear to be an integer value.");
				}
			}
		}
	}

	/**
	 * Retrieves a string representation of this Gene that includes any
	 * information required to reconstruct it at a later time, such as its value
	 * and internal state. This string will be used to represent this Gene in
	 * XML persistence. This is an optional method but, if not implemented, XML
	 * persistence and possibly other features will not be available. An
	 * UnsupportedOperationException should be thrown if no implementation is
	 * provided.
	 * 
	 * @return a string representation of this Gene's current state
	 * 
	 * @throws UnsupportedOperationException
	 *             to indicate that no implementation is provided for this
	 *             method
	 * 
	 * @author Neil Rostan
	 * @since 1.0
	 */
	public String getPersistentRepresentation()
			throws UnsupportedOperationException {
		// The persistent representation includes the value, lower bound,
		// and upper bound. Each is separated by a colon.
		// --------------------------------------------------------------
		Iterator<V> it = m_geneSet.iterator();
		StringBuffer strbf = new StringBuffer();
		while (it.hasNext()) {
			strbf.append(PERSISTENT_FIELD_DELIMITER);
			strbf.append(it.next().toString());
		}
		return m_value.toString() + strbf.toString();
	}

	/**
	 * Sets the value (allele) of this Gene to the new given value. This class
	 * expects the value to be an instance of current type (e.g. Integer).
	 * 
	 * @param a_newValue
	 *            the new value of this Gene instance.
	 * 
	 * @author Johnathan Kool
	 */
	@SuppressWarnings("unchecked")
	public void setAllele(Object a_newValue) {
		if (m_geneSet.contains(a_newValue)) {
			m_value = (V) a_newValue;
		} else if (a_newValue == null && this.m_geneSet.isEmpty()) {
			m_value = null;
		} else {
			throw new IllegalArgumentException(
					"Allele value being set is not an "
							+ "element of the set of permitted" + " values.");
		}
	}

	/**
	 * Compares this NumberGene with the specified object (which must also be a
	 * NumberGene) for order, which is determined by the number value of this
	 * Gene compared to the one provided for comparison.
	 * 
	 * @param other
	 *            the NumberGene to be compared to this NumberGene
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the object provided for
	 *         comparison
	 * 
	 * @throws ClassCastException
	 *             if the specified object's type prevents it from being
	 *             compared to this NumberGene
	 * 
	 * @author Klaus Meffert
	 * @author Johnathan Kool
	 * @since 2.4
	 */
	@SuppressWarnings("unchecked")
	public int compareTo(Object other) {
		BetterSetGene<V> otherGene = (BetterSetGene<V>) other;
		// First, if the other gene (or its value) is null, then this is
		// the greater allele. Otherwise, just use the overridden
		// compareToNative
		// method to perform the comparison.
		// ---------------------------------------------------------------
		if (otherGene == null) {
			return 1;
		} else if (otherGene.m_value == null) {
			// If our value is also null, then we're the same. Otherwise,
			// this is the greater gene.
			// ----------------------------------------------------------
			return m_value == null ? 0 : 1;
		} else {
			if (m_value == null) {
				// we already know the other value isn't null
				return 1;
			}
			ICompareToHandler handler = getConfiguration().getJGAPFactory()
					.getCompareToHandlerFor(m_value, m_value.getClass());
			if (handler != null) {
				try {
					return ((Integer) handler.perform(m_value, null,
							otherGene.m_value)).intValue();
				} catch (Exception ex) {
					throw new Error(ex);
				}
			} else {
				return 0;
			}
		}
	}

	/**
	 * @return the internal value of the gene
	 * @since 2.4
	 */
	protected Object getInternalValue() {
		return m_value;
	}

	/**
	 * Modified hashCode() function to return different hashcodes for
	 * differently ordered genes in a chromosome
	 * 
	 * @return -67 if no allele set, otherwise value return by
	 *         BaseGene.hashCode()
	 * 
	 * @author Klaus Meffert
	 * @since 2.4
	 */
	public int hashCode() {
		if (getInternalValue() == null) {
			return -67;
		} else {
			return super.hashCode();
		}
	}
}
