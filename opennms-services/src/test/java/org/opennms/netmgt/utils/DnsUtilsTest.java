/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.utils;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.junit.Ignore;
import org.junit.Test;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Type;

public class DnsUtilsTest {

    @Test
    @Ignore
    public void testPreferIPv6() throws Exception {
    	DnsUtils.resolveHostname("ipv4.www.yahoo.com", false);
        try {
        	DnsUtils.resolveHostname("ipv4.www.yahoo.com", true);
            fail();
        } catch (UnknownHostException e) {
            // Expected exception
        }
        try {
            DnsUtils.resolveHostname("ipv6.www.yahoo.com", false);
            fail();
        } catch (UnknownHostException e) {
            // Expected exception
        }
        DnsUtils.resolveHostname("ipv6.www.yahoo.com", true);
    }

    @Test
    @Ignore
    public void testLookup() throws Exception {
        InetAddress fb = DnsUtils.resolveHostname("www.opennms.org", false);
        assertNotNull(fb);
    }

    /**
     * Make sure this test is FIRST.
     */
    @Test
    @Ignore
    public void testOrderingOfLookups() throws Exception {
        //String lookup = "www.opennms.org";
        String lookup = "www.facebook.com";
        Record[] fb = new Lookup(lookup, Type.AAAA).run();
        fb = new Lookup(lookup, Type.A).run();
        assertNotNull(fb);
    }

}