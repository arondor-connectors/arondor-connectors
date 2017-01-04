package com.arondor.viewer.jdbc.annotation;

/**
 * Defines an annotation version
 * 
 * @author Christopher Laszczuk
 * 
 */
public class AnnotationVersion
{
    private int id;

    private long timeStamp;

    public AnnotationVersion(int id, long timeStamp)
    {
        this.id = id;
        this.timeStamp = timeStamp;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public long getTimeStamp()
    {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

    public String toString()
    {
        return "AnnotationVersion[id=" + id + ", timeStamp=" + timeStamp + "]";
    }
}
