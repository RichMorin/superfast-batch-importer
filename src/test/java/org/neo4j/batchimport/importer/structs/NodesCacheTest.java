package org.neo4j.batchimport.importer.structs;

import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class NodesCacheTest
{
    @Test
    public void shouldStoreAndRetrieveRelationshipId() throws Exception
    {
        // given
        NodesCache cache = new NodesCache( 100, null );

        // when
        cache.put( 1, 2 );

        // then
        long retrievedRelationshipId = cache.get( 1 );
        assertEquals( 2, retrievedRelationshipId );
    }

    @Test
    public void shouldRetrieveUnsetRelationshipId() throws Exception
    {
        // given
        NodesCache cache = new NodesCache( 100, null );

        // then
        long retrievedRelationshipId = cache.get( 1 );
        assertEquals( -1, retrievedRelationshipId );
    }

    @Test
    public void shouldCountRelationships() throws Exception
    {
        // given
        NodesCache cache = new NodesCache( 100, null );

        // when
        assertEquals( 0, cache.getCount( 1 ) );
        cache.incrementCount( 1 );
        assertEquals( 1, cache.getCount( 1 ) );
        cache.incrementCount( 1 );
        assertEquals( 2, cache.getCount( 1 ) );
        cache.incrementCount( 1 );
        assertEquals( 3, cache.getCount( 1 ) );

        cache.decrementCount( 1 );
        assertEquals( 2, cache.getCount( 1 ) );
        cache.decrementCount( 1 );
        assertEquals( 1, cache.getCount( 1 ) );
        cache.decrementCount( 1 );
        assertEquals( 0, cache.getCount( 1 ) );
    }

    @Test
    public void shouldPreventNegativeCount() throws Exception
    {
        // given
        NodesCache cache = new NodesCache( 100, null );
        assertEquals( 0, cache.getCount( 1 ) );

        // when
        try
        {
            cache.decrementCount( 1 );
            fail( "Should have thrown exception" );
        }
        catch ( IllegalStateException e )
        {
            // expected
        }
    }

    @Test
    public void shouldPreventOverflowCount() throws Exception
    {
        // given
        NodesCache cache = new NodesCache( 100, null );
        cache.changeCount( 1, cache.maxCount() );
        assertEquals( Math.pow( 2, 28 ) - 1, cache.getCount( 1 ), 0.1 );

        // when
        try
        {
            cache.incrementCount( 1 );
            fail( "Should have thrown exception" );
        }
        catch ( IllegalStateException e )
        {
            // expected
        }
    }

    @Test
    public void shouldVisitNodeOnce() throws Exception
    {
        // GIVEN
        NodesCache cache = new NodesCache( 100, null );

        // THEN
        assertFalse( cache.checkAndSetVisited( 1 ) );
        assertTrue( cache.checkAndSetVisited( 1 ) );
        assertTrue( cache.checkAndSetVisited( 1 ) );

        assertFalse( cache.checkAndSetVisited( 2 ) );
        assertTrue( cache.checkAndSetVisited( 2 ) );
        assertTrue( cache.checkAndSetVisited( 2 ) );
    }

    @Test
    public void visitBitShouldNotAffectCount() throws Exception
    {
        // GIVEN
        NodesCache cache = new NodesCache( 100, null );
        int count = cache.maxCount();
        cache.changeCount( 1, count );

        // WHEN/THEN
        assertFalse( cache.checkAndSetVisited( 1 ) );
        assertEquals( count, cache.getCount( 1 ) );
        assertTrue( cache.checkAndSetVisited( 1 ) );
        assertEquals( count, cache.getCount( 1 ) );
        cache.decrementCount( 1 );
        assertTrue( cache.checkAndSetVisited( 1 ) );
        assertEquals( count-1, cache.getCount( 1 ) );
    }

    @Test
    public void idChangeShouldNotAffectOtherBits() throws Exception
    {
        // GIVEN
        NodesCache cache = new NodesCache( 100, null );
        long nodeId = 1;
        cache.put( nodeId, 10 );
        cache.changeCount( nodeId, 123 );
        cache.checkAndSetVisited( nodeId );

        // WHEN
        assertEquals( 10, cache.put( nodeId, 11 ) );

        // THEN
        assertEquals( 123, cache.getCount( nodeId ) );
        assertTrue( cache.checkAndSetVisited( nodeId ) );
    }
}
