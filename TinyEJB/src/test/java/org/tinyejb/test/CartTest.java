package org.tinyejb.test;

import javax.naming.Context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tinyejb.core.EJBContainer;
import org.tinyejb.core.IJndiResolver;
import org.tinyejb.core.JBossJndiResolver;
import org.tinyejb.core.ResourceHolder;
import org.tinyejb.test.ejbs.stateful.CartItem;
import org.tinyejb.test.ejbs.stateful.CartLocal;
import org.tinyejb.test.ejbs.stateful.CartLocalHome;
import org.tinyejb.test.mocks.MockNamingContext;
import org.tinyejb.test.mocks.MockTransacionManager;

/**
 * Demonstrates the use of TinyEJB container with stateful session bean
 * 
 * @author Cláudio Gualberto
 * 20/09/2014
 *
 */
public class CartTest {
	private final static Logger LOGGER = LoggerFactory.getLogger(CartTest.class);
	private EJBContainer ejbContainer;
	private Context jndiContext;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			new CartTest().test();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void test() throws Exception {

		initContainer();

		LOGGER.debug("\n\n**** CART BEAN (STATEFUL) ****\n");

		//On this example, we'll use local interfaces
		CartLocalHome home = (CartLocalHome) jndiContext.lookup(CartLocalHome.JNDI_NAME);

		//create an instance to this specific client
		CartLocal cart = home.create("Stephen Hawking");

		//let's add some items on the shopping cart
		cart.addItem(new CartItem("Book", 2, "The Universer and everything else", 45.35));
		cart.addItem(new CartItem("Book", 1, "Are there really black holes?", 110.50));
		cart.addItem(new CartItem("Blue-ray disc", 2, "Science against God", 25.99));

		//call listing method
		cart.listItems();

		System.out.printf("\nTotal amount: %.2f\n\n", cart.getTotalAmount());

		cart.remove();

		ejbContainer.undeploy();
	}

	private void initContainer() throws Exception {
		jndiContext = new MockNamingContext(); //very simple naming context (test purpose only)

		ResourceHolder.setHolder(new MockTransacionManager(), jndiContext);
		ejbContainer = new EJBContainer();

		/*
		 using JBoss deployment descriptor to define jndi names. 
		 if we don't use a special resolver, TinyEJB will bind EJB home proxies on JNDI using names as defined on EJB 2.1 spec.
		 for example:
		      Cart bean's local home would be on 'java:comp/env/ejb/{localHomeSimpleName}' 
		      Cart bean's remote home would be on 'java:comp/env/ejb/{remoteHomeSimpleName}' 
		      
		 In this example, the file is named tinyjboss.xml, but naturally it can be named as you can, but it must be compliant with jboss_4_0.dtd     
		 */
		IJndiResolver jndi = JBossJndiResolver.buildFromJBossDescriptor(CartTest.class.getResourceAsStream("/tinyejb-jboss.xml"));

		ejbContainer.setJndiResolver(jndi);

		/*
		   client code must locate and load ejb-jar.xml from classpath or wherever it can be.
		   Naturally, the file name is irrelevant for TinyEJB container, but it must be, at least, compliant with EJB 2.0 XML DTD or EJB 2.1 schema  
		*/
		ejbContainer.deployModuleFromDescriptor(CartTest.class.getResourceAsStream("/tinyejb-ejb-jar.xml"));

	}
}
