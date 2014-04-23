package org.neo4j.batchimport.importer.stages;

import java.lang.reflect.Method;

import org.neo4j.batchimport.importer.structs.Constants;
import org.neo4j.batchimport.importer.structs.DiskBlockingQ;
import org.neo4j.batchimport.importer.structs.DiskRecordsCache;
import org.neo4j.batchimport.importer.structs.RunData;

public class WriterStage
{
    protected int numWriters = 0;
    protected DiskRecordsCache diskRecCache;
    protected DiskBlockingQ diskBlockingQ;
    protected WriterWorker[] writerWorker = null;
    protected Method[] writerMethods = null;
    protected RunData[] writerRunData;
    protected Stages stages;

    public WriterStage( Stages stages )
    {
        this.stages = stages;
        diskRecCache = new DiskRecordsCache( Constants.BUFFERQ_SIZE * 2, Constants.BUFFER_ENTRIES );
        diskBlockingQ = new DiskBlockingQ( 3, Constants.BUFFERQ_SIZE );
        stages.registerWriterStage( this );
    }

    public void init( Method... methods )
    {
        this.numWriters = methods.length;
        writerWorker = new WriterWorker[numWriters];
        writerMethods = new Method[numWriters];
        writerRunData = new RunData[numWriters];
        for ( int i = 0; i < methods.length; i++ )
        {
            writerMethods[i] = methods[i];
        }
        for ( int i = 0; i < numWriters; i++ )
        {
            int type = 0;
            if ( writerMethods[i].getName().contains( "Property" ) )
            {
                type = Constants.PROPERTY;
            }
            else if ( writerMethods[i].getName().contains( "Node" ) )
            {
                type = Constants.NODE;
            }
            else if ( writerMethods[i].getName().contains( "Relationship" ) )
            {
                type = Constants.RELATIONSHIP;
            }
            writerWorker[i] = new WriterWorker( i, type, stages, this );
            writerRunData[i] = new RunData( "Writer" + i );
        }
    }

    public void start()
    {
        for ( int i = 0; i < numWriters; i++ )
        {
            writerWorker[i].start();
        }
    }

    public void stop()
    {
        for ( int i = 0; i < numWriters; i++ )
        {
            writerWorker[i].stop();
        }
    }

    public DiskRecordsCache getDiskRecordsCache()
    {
        return diskRecCache;
    }

    public DiskBlockingQ getDiskBlockingQ()
    {
        return diskBlockingQ;
    }

    public RunData getRunData( int index )
    {
        return writerRunData[index];
    }

}
