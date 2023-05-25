package gitlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Date;

public class Commit implements Serializable {


    /**
     * This is an initial constructor.
     */
    public Commit() {
        _timeStamp = new Date(0L);
        _message = "initial commit";
        _commitParentHash = null;
        _hashID = Utils.sha1();
        _connectedBlobs = new HashMap<>();
    }

    public Commit(String typeInFailure) {
        _message = null;
    }

    /**
     * This is a commit constructor when someone is calling a commit later.
     *
     * @param message     The message associated with the commit
     * @param parentHash  The parent of the commit
     * @param parentBlobs blobs of all the parents before it
     */
    public Commit(String parentHash, String message, HashMap<String,
            String> parentBlobs) {
        _commitParentHash = parentHash;
        _message = message;
        _timeStamp = new Date();
        _connectedBlobs = parentBlobs;
    }

    /**
     * This adds a blob to the array list of Blobs.
     *
     * @param fileName The name of the file to add
     * @param hashVal  the hash value of the file to add
     */
    public void addBlob(String fileName, String hashVal) {
        getConnectedBlobs().put(fileName, hashVal);
    }

    /**
     * @return The connected blobs for the commit
     */
    public HashMap<String, String> getConnectedBlobs() {
        return _connectedBlobs;
    }

    /**
     * Hello.
     * Sets the HashID of the commit
     *
     * @param hashID What the _hashID should be set to
     */
    public void setHashID(String hashID) {
        _hashID = hashID;
    }

    /**
     * @return the time stamp of the commit
     */
    public Date getTimeStamp() {
        return _timeStamp;
    }

    /**
     * @return the parents hash of the commit
     */
    public String getCommitParentHash() {
        return _commitParentHash;
    }

    /**
     * @return the current Hash of the commit
     */
    public String getHashID() {
        return _hashID;
    }

    /**
     * @return the message of the commit
     */
    public String getMessage() {
        return _message;
    }

    /**
     * This is the message within the commit.
     */
    private String _message;

    /**
     * This is the timeStamp for the commit.
     */
    private Date _timeStamp;

    /**
     * This is the Parent commits.
     */
    private String _commitParentHash;

    /**
     * This is the hashValue of the commit.
     */
    private String _hashID;

    /**
     * this is a Hashmap of all blobs that the commit keeps track of,
     * it tracks the name of the blob to the Hash.
     */
    private HashMap<String, String> _connectedBlobs;
}
