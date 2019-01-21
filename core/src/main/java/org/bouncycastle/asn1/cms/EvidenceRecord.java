package org.bouncycastle.asn1.cms;

import java.util.Enumeration;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.util.BigIntegers;

/**
 * <a href="https://tools.ietf.org/html/rfc4998">RFC 4998</a>:
 * Evidence Record Syntax (ERS)
 * <p>
 * <pre>
 * EvidenceRecord ::= SEQUENCE {
 *   version                   INTEGER { v1(1) } ,
 *   digestAlgorithms          SEQUENCE OF AlgorithmIdentifier,
 *   cryptoInfos               [0] CryptoInfos OPTIONAL,
 *   encryptionInfo            [1] EncryptionInfo OPTIONAL,
 *   archiveTimeStampSequence  ArchiveTimeStampSequence
 * }
 *
 * CryptoInfos ::= SEQUENCE SIZE (1..MAX) OF Attribute
 * </pre>
 */
public class EvidenceRecord
    extends ASN1Object
{

    /**
     * ERS {iso(1) identified-organization(3) dod(6) internet(1) security(5) mechanisms(5) ltans(11)
     * id-mod(0) id-mod-ers88(2) id-mod-ers88-v1(1) }
     */
    private static final ASN1ObjectIdentifier OID = new ASN1ObjectIdentifier("1.3.6.1.5.5.11.0.2.1");

    private ASN1Integer version = new ASN1Integer(1);
    private ASN1Sequence digestAlgorithms;
    private CryptoInfos cryptoInfos;
    private EncryptionInfo encryptionInfo;
    private ArchiveTimeStampSequence archiveTimeStampSequence;

    /**
     * Return an EvidenceRecord from the given object.
     *
     * @param obj the object we want converted.
     * @return an EvidenceRecord instance, or null.
     * @throws IllegalArgumentException if the object cannot be converted.
     */
    public static EvidenceRecord getInstance(final Object obj)
    {
        if (obj instanceof EvidenceRecord)
        {
            return (EvidenceRecord)obj;
        }
        else if (obj != null)
        {
            return new EvidenceRecord(ASN1Sequence.getInstance(obj));
        }

        return null;
    }

    public EvidenceRecord(
        final ASN1Sequence digestAlgorithms,
        final CryptoInfos cryptoInfos,
        final EncryptionInfo encryptionInfo,
        final ArchiveTimeStampSequence archiveTimeStampSequence)
    {
        Enumeration digestAlgos = digestAlgorithms.getObjects();

        while (digestAlgos.hasMoreElements())
        {
            final Object digestAlgo = digestAlgos.nextElement();
            if (!(digestAlgo instanceof AlgorithmIdentifier))
            {
                throw new IllegalArgumentException("unknown object in getInstance: " +
                    digestAlgo.getClass().getName());
            }
        }

        this.digestAlgorithms = digestAlgorithms;
        this.cryptoInfos = cryptoInfos;
        this.encryptionInfo = encryptionInfo;
        this.archiveTimeStampSequence = archiveTimeStampSequence;
    }

    private EvidenceRecord(final ASN1Sequence sequence)
    {
        if (sequence.size() < 3 && sequence.size() > 5)
        {
            throw new IllegalArgumentException("wrong sequence size in constructor: " + sequence.size());
        }

        final ASN1Integer versionNumber = ASN1Integer.getInstance(sequence.getObjectAt(0));
        if (!versionNumber.getValue().equals(BigIntegers.ONE))
        {
            throw new IllegalArgumentException("incompatible version");
        }
        else
        {
            this.version = versionNumber;
        }

        this.digestAlgorithms = ASN1Sequence.getInstance(sequence.getObjectAt(1));
        for (int i = 2; i != sequence.size() - 1; i++)
        {
            ASN1Encodable object = sequence.getObjectAt(i);

            if (object instanceof ASN1TaggedObject)
            {
                ASN1TaggedObject asn1TaggedObject = (ASN1TaggedObject)object;
                switch (asn1TaggedObject.getTagNo())
                {
                case 0:
                    cryptoInfos = CryptoInfos.getInstance(asn1TaggedObject, false);
                    break;
                case 1:
                    encryptionInfo = EncryptionInfo.getInstance(asn1TaggedObject, false);
                    break;
                default:
                    throw new IllegalArgumentException("unknown tag in getInstance: " + asn1TaggedObject.getTagNo());
                }
            }
            else
            {
                throw new IllegalArgumentException("unknown object in getInstance: " +
                    object.getClass().getName());
            }
        }
        archiveTimeStampSequence = ArchiveTimeStampSequence.getInstance(sequence.getObjectAt(sequence.size() - 1));
    }

    public AlgorithmIdentifier[] getDigestAlgorithms()
    {
        AlgorithmIdentifier[] rv = new AlgorithmIdentifier[digestAlgorithms.size()];

        for (int i = 0; i != rv.length; i++)
        {
            rv[i] = AlgorithmIdentifier.getInstance(digestAlgorithms.getObjectAt(i));
        }

        return rv;
    }

    public ArchiveTimeStampSequence getArchiveTimeStampSequence()
    {
        return archiveTimeStampSequence;
    }

    public String toString()
    {
        return ("EvidenceRecord: Oid(" + OID + ")");
    }

    public ASN1Primitive toASN1Primitive()
    {
        final ASN1EncodableVector vector = new ASN1EncodableVector();

        vector.add(version);
        vector.add(digestAlgorithms);

        if (null != cryptoInfos)
        {
            vector.add(cryptoInfos);
        }
        if (null != encryptionInfo)
        {
            vector.add(encryptionInfo);
        }

        vector.add(archiveTimeStampSequence);

        return new DERSequence(vector);
    }
}