package org.bouncycastle.crypto.random;

import java.security.SecureRandom;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.Mac;
import org.bouncycastle.crypto.prng.BasicEntropySourceProvider;
import org.bouncycastle.crypto.prng.CTRSP800DRBG;
import org.bouncycastle.crypto.prng.DualECSP800DRBG;
import org.bouncycastle.crypto.prng.EntropySource;
import org.bouncycastle.crypto.prng.EntropySourceProvider;
import org.bouncycastle.crypto.prng.HMacSP800DRBG;
import org.bouncycastle.crypto.prng.HashSP800DRBG;
import org.bouncycastle.crypto.prng.SP80090DRBG;

/**
 * Builder class for making SecureRandom objects based on SP 800-90A Deterministic Random Bit Generators (DRBG).
 */
public class SP800SecureRandomBuilder
{
    private final SecureRandom random;
    private final EntropySourceProvider entropySourceProvider;

    private byte[] personalizationString;
    private int securityStrength = 256;
    private int entropyBitsRequired = 256;

    /**
     * Basic constructor, creates a builder using an EntropySourceProvider based on the default SecureRandom with
     * predictionResistant set to false.
     * <p>
     * Any SecureRandom created from a builder constructed like this will make use of input passed to SecureRandom.setSeed() if
     * the passed in SecureRandom does.
     * </p>
     */
    public SP800SecureRandomBuilder()
    {
        this(new SecureRandom(), false);
    }

    /**
     * Construct a builder with an EntropySourceProvider based on the passed in SecureRandom and the passed in value
     * for prediction resistance.
     * <p>
     * Any SecureRandom created from a builder constructed like this will make use of input passed to SecureRandom.setSeed() if
     * the passed in SecureRandom does.
     * </p>
     * @param entropySource
     * @param predictionResistant
     */
    public SP800SecureRandomBuilder(SecureRandom entropySource, boolean predictionResistant)
    {
        this.random = entropySource;
        this.entropySourceProvider = new BasicEntropySourceProvider(random, predictionResistant);
    }

    /**
     * Create a builder which makes creates the SecureRandom objects from a specified entropy source provider.
     * <p>
     *     <b>Note:</b> If this constructor is used any calls to setSeed() in the resulting SecureRandom will be ignored.
     * </p>
     * @param entropySourceProvider a provider of EntropySource objects.
     */
    public SP800SecureRandomBuilder(EntropySourceProvider entropySourceProvider)
    {
        this.random = null;
        this.entropySourceProvider = entropySourceProvider;
    }

    public SP800SecureRandomBuilder setPersonalizationString(byte[] personalizationString)
    {
        this.personalizationString = personalizationString;

        return this;
    }

    public SP800SecureRandomBuilder setSecurityStrength(int securityStrength)
    {
        this.securityStrength = securityStrength;

        return this;
    }

    public SP800SecureRandomBuilder setEntropyBitsRequired(int entropyBitsRequired)
    {
        this.entropyBitsRequired = entropyBitsRequired;

        return this;
    }

    /**
     * Build a SecureRandom based on a SP 800-90A Hash DRBG.
     *
     * @param digest digest algorithm to use in the DRBG underneath the SecureRandom.
     * @param nonce  nonce value to use in DRBG construction.
     * @param predictionResistant specify whether the underlying DRBG in the resulting SecureRandom should reseed on each request for bytes.
     * @return a SecureRandom supported by a Hash DRBG.
     */
    public SP800SecureRandom buildHash(Digest digest, byte[] nonce, boolean predictionResistant)
    {
        return new SP800SecureRandom(random, entropySourceProvider.get(entropyBitsRequired), new HashDRBGProvider(digest, nonce, personalizationString, securityStrength), predictionResistant);
    }

    public SP800SecureRandom build(BlockCipher cipher, int keySizeInBits, int seedLength, byte[] nonce, boolean predictionResistant)
    {
        return new SP800SecureRandom(random, entropySourceProvider.get(entropyBitsRequired), new CTRDRBGProvider(cipher, keySizeInBits, nonce, personalizationString, securityStrength), predictionResistant);
    }

    /**
     * Build a SecureRandom based on a SP 800-90A HMAC DRBG.
     *
     * @param hMac HMAC algorithm to use in the DRBG underneath the SecureRandom.
     * @param nonce  nonce value to use in DRBG construction.
     * @param predictionResistant specify whether the underlying DRBG in the resulting SecureRandom should reseed on each request for bytes.
     * @return a SecureRandom supported by a HMAC DRBG.
     */
    public SP800SecureRandom buildHMAC(Mac hMac, byte[] nonce, boolean predictionResistant)
    {
        return new SP800SecureRandom(random, entropySourceProvider.get(entropyBitsRequired), new HMacDRBGProvider(hMac, nonce, personalizationString, securityStrength), predictionResistant);
    }

    /**
     * Build a SecureRandom based on a SP 800-90A Dual EC DRBG.
     *
     * @param digest digest algorithm to use in the DRBG underneath the SecureRandom.
     * @param nonce  nonce value to use in DRBG construction.
     * @param predictionResistant specify whether the underlying DRBG in the resulting SecureRandom should reseed on each request for bytes.
     * @return a SecureRandom supported by a Dual EC DRBG.
     */
    public SP800SecureRandom buildDualEC(Digest digest, byte[] nonce, boolean predictionResistant)
    {
        return new SP800SecureRandom(random, entropySourceProvider.get(entropyBitsRequired), new DualECDRBGProvider(digest, nonce, personalizationString, securityStrength), predictionResistant);
    }

    private static class HashDRBGProvider
        implements DRBGProvider
    {
        private final Digest digest;
        private final byte[] nonce;
        private final byte[] personalizationString;
        private final int securityStrength;

        public HashDRBGProvider(Digest digest, byte[] nonce, byte[] personalizationString, int securityStrength)
        {
            this.digest = digest;
            this.nonce = nonce;
            this.personalizationString = personalizationString;
            this.securityStrength = securityStrength;
        }

        public SP80090DRBG get(EntropySource entropySource)
        {
            return new HashSP800DRBG(digest, entropySource, nonce, personalizationString, securityStrength);
        }
    }

    private static class DualECDRBGProvider
        implements DRBGProvider
    {
        private final Digest digest;
        private final byte[] nonce;
        private final byte[] personalizationString;
        private final int securityStrength;

        public DualECDRBGProvider(Digest digest, byte[] nonce, byte[] personalizationString, int securityStrength)
        {
            this.digest = digest;
            this.nonce = nonce;
            this.personalizationString = personalizationString;
            this.securityStrength = securityStrength;
        }

        public SP80090DRBG get(EntropySource entropySource)
        {
            return new DualECSP800DRBG(digest, entropySource, nonce, personalizationString, securityStrength);
        }
    }

    private static class HMacDRBGProvider
        implements DRBGProvider
    {
        private final Mac hMac;
        private final byte[] nonce;
        private final byte[] personalizationString;
        private final int securityStrength;

        public HMacDRBGProvider(Mac hMac, byte[] nonce, byte[] personalizationString, int securityStrength)
        {
            this.hMac = hMac;
            this.nonce = nonce;
            this.personalizationString = personalizationString;
            this.securityStrength = securityStrength;
        }

        public SP80090DRBG get(EntropySource entropySource)
        {
            return new HMacSP800DRBG(hMac, entropySource, nonce, personalizationString, securityStrength);
        }
    }

    private static class CTRDRBGProvider
        implements DRBGProvider
    {

        private final BlockCipher blockCipher;
        private final int keySizeInBits;
        private final byte[] nonce;
        private final byte[] personalizationString;
        private final int securityStrength;

        public CTRDRBGProvider(BlockCipher blockCipher, int keySizeInBits, byte[] nonce, byte[] personalizationString, int securityStrength)
        {
            this.blockCipher = blockCipher;
            this.keySizeInBits = keySizeInBits;
            this.nonce = nonce;
            this.personalizationString = personalizationString;
            this.securityStrength = securityStrength;
        }

        public SP80090DRBG get(EntropySource entropySource)
        {
            return new CTRSP800DRBG(blockCipher, keySizeInBits, entropySource, nonce, personalizationString, securityStrength);
        }
    }
}
