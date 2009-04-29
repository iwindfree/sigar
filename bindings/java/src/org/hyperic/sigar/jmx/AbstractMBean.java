/*
 * Copyright (C) [2004, 2005, 2006, 2007], Hyperic, Inc.
 * This file is part of SIGAR.
 * 
 * SIGAR is free software; you can redistribute it and/or modify
 * it under the terms version 2 of the GNU General Public License as
 * published by the Free Software Foundation. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package org.hyperic.sigar.jmx;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.MBeanException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;
import org.hyperic.sigar.SigarProxy;

/**
 * Base class for all Sigar JMX MBeans. Provides a skeleton which handles
 * creation of the Sigar proxy instance and provides some convenience methods.
 * It also enforces usage of the {@link DynamicMBean} inferface while
 * implementing part of it, and it adds empty implementations for all methods of
 * the {@link MBeanRegistration} interface, allowing subclasses to only
 * implement subset of them.
 * 
 * @author Bjoern Martin
 * @since 1.5
 */
public abstract class AbstractMBean implements DynamicMBean, MBeanRegistration {

    public static final String MBEAN_DOMAIN = SigarInvokerJMX.DOMAIN_NAME;
    public static final String MBEAN_ATTR_TYPE = SigarInvokerJMX.PROP_TYPE;

    /**
     * The Sigar proxy cache to be used in case the data does not have to be 
     * fetched during each call. The cache timeout is decided during 
     * construction. See {@link AbstractMBean#AbstractMBean(Sigar, short)} for 
     * details.
     * 
     * @see AbstractMBean#AbstractMBean(Sigar, short)
     */
    protected final SigarProxy sigar;

    /**
     * The MBean server this MBean is registered to. Set during the MBean's 
     * registration to the MBean server and unset to <code>null</code> when
     * the deregistration finished.
     * 
     * @see #preRegister(MBeanServer, ObjectName)
     * @see #postDeregister()
     */
    protected MBeanServer mbeanServer;

    /**
     * <p>Creates a new instance of this class. The SigarProxy instance is stored (and 
     * accessible) via the {@link #sigar} member.
     * 
     * @param sigar The SigarProxy instance to use. Must not be <code>null</code>
     */
    protected AbstractMBean(SigarProxy sigar) {
        this.sigar = sigar;
    }

    /**
     * Returns the object name the MBean is registered with within the
     * MBeanServer. May be <code>null</code> in case the instance is not
     * registered to an MBeanServer, but used standalone.
     * 
     * @return The object name or <code>null</code> if not registered to an
     *         MBeanServer
     */
    public abstract String getObjectName();

    /**
     * Returns a runtime exception for the type and SigarException specified.
     * 
     * @param type
     *            The type that was called
     * @param e
     *            The exception that was raised
     * @return A runtime exception encapsulating the information specified
     */
    protected RuntimeException unexpectedError(String type, SigarException e) {
        String msg = "Unexected error in Sigar.get" + type + ": "
                + e.getMessage();
        return new IllegalArgumentException(msg);
    }

    /**
     * Loops over all attributes and calls
     * {@link DynamicMBean#getAttribute(java.lang.String)} method for each
     * attribute sequentially. Any exception thrown by those methods are ignored
     * and simply cause the attribute not being added to the result.
     */
    public AttributeList getAttributes(String[] attrs) {
        final AttributeList result = new AttributeList();
        for (int i = 0; i < attrs.length; i++) {
            try {
                result.add(new Attribute(attrs[i], getAttribute(attrs[i])));
            } catch (AttributeNotFoundException e) {
                // ignore, as we cannot throw this exception
            } catch (MBeanException e) {
                // ignore, as we cannot throw this exception
            } catch (ReflectionException e) {
                // ignore, as we cannot throw this exception
            }
        }
        return result;
    }

    /**
     * Loops over all attributes and calls
     * {@link DynamicMBean#setAttribute(Attribute)} for each attribute
     * sequentially. Any exception thrown by those methods are ignored and
     * simply cause the attribute not being added to the result.
     */
    public AttributeList setAttributes(AttributeList attrs) {
        final AttributeList result = new AttributeList();
        for (int i = 0; i < attrs.size(); i++) {
            try {
                final Attribute next = (Attribute) attrs.get(i);
                setAttribute(next);
                result.add(next);
            } catch (AttributeNotFoundException e) {
                // ignore, as we cannot throw this exception
            }
        }
        return result;
    }

    // -------
    // Implementation of the MBeanRegistration interface
    // -------

    /**
     * <p>Returns <code>new ObjectName(this.getObjectName())</code> to guarantee 
     * a reliable and reproducable object name.</p>
     * 
     * <p><b>Note:</b> Make sure any subclass does a super call to this method, 
     * otherwise the implementation might be broken.</p>
     * 
     * @see MBeanRegistration#preRegister(MBeanServer, ObjectName)
     */
    public ObjectName preRegister(MBeanServer server, ObjectName name)
            throws Exception {
        this.mbeanServer = server;
        return new ObjectName(getObjectName());
    }

    /**
     * Empty implementation, allowing subclasses to ignore the interface.
     * 
     * <p><b>Note:</b> Make sure any subclass does a super call to this method, 
     * otherwise the implementation might be broken.</p>
     * 
     * @see MBeanRegistration#postRegister(Boolean)
     */
    public void postRegister(Boolean success) {
    }

    /**
     * Empty implementation, allowing subclasses to ignore the interface.
     * 
     * <p><b>Note:</b> Make sure any subclass does a super call to this method, 
     * otherwise the implementation might be broken.</p>
     * 
     * @see MBeanRegistration#preDeregister()
     */
    public void preDeregister() throws Exception {
    }

    /**
     * Empty implementation, allowing subclasses to ignore the interface.
     * 
     * <p><b>Note:</b> Make sure any subclass does a super call to this method, 
     * otherwise the implementation might be broken.</p>
     * 
     * @see MBeanRegistration#postDeregister()
     */
    public void postDeregister() {
        this.mbeanServer = null;
    }

    public void setAttribute(Attribute attr) throws AttributeNotFoundException {
        throw new AttributeNotFoundException(attr.getName());
    }

    public Object invoke(String name, Object[] params, String[] signature)
        throws ReflectionException {

        throw new ReflectionException(new NoSuchMethodException(name),
                                      name);
    }
}
