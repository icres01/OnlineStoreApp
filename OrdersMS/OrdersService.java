package com.ibm.ws.msdemo.rest;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.ibm.ws.msdemo.rest.pojo.Order;

/**
 * This class handles post or get requests from UI application
 * and publishes order information to the Shipping application.
 *
 */
//Mapped to /orders via web.xml
@Path("/orders")
public class OrdersService {

    /** Transaction Manager */
	private UserTransaction utx;
	
	/** Entity Manager */
	private EntityManager em;
    
    /** Simple logging */
    private final static Logger logger = Logger.getLogger(OrdersService.class.getName());

	public OrdersService(){
		
		utx = getUserTransaction();
		em = getEm();

		logger.log(Level.INFO,"Initialising...");
		
        logger.log(Level.INFO,"Completed initialisation.");
	}

	@Context UriInfo uriInfo;
	
	 /**
     * GET all orders
     * @return Response object
     */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response get() {
		
			List<Order> list = em.createQuery("SELECT t FROM Order t", Order.class).getResultList();
			String json = list.toString();
			return Response.ok(json).build();
	}
	
	/**
     * GET a specific order
     * @param id
     * @return Order
     */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("{id}")
	public Response get(@PathParam("id") long id) {	
		
		System.out.println("Searching for id : " + id);
		
		Order order = null;
		try {
			order = em.find(Order.class, id);
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).build();
		} 
		
		if (order != null) {
			return Response.ok(order.toString()).build();
		} else {
			return Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).build();
		}
	}
	
	/**
     * Create new order
     * @param order
     * @return id of created order
     */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response create(Order order) {
		
		System.out.println("New order: " + order.toString());
		
		try {
			utx.begin();
			em.persist(order);
			utx.commit();
			
			return Response.status(javax.ws.rs.core.Response.Status.CREATED).entity(String.valueOf(order.getId())).build();
			
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR).build();
			
		} finally {
			try {
				if (utx.getStatus() == javax.transaction.Status.STATUS_ACTIVE) {
					utx.rollback();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	// The Liberty buildpack automatically generates server.xml configuration 
	// stanzas for the SQL database service which contain the credentials needed to 
	// connect to the service. The buildpack generates a JNDI name following  
	// the convention of "jdbc/<service_name>" where the <service_name> is the 
	// name of the bound service. 
	// Below we'll do a JNDI lookup for the EntityManager whose persistence 
	// context is defined in web.xml. It references a persistence unit defined 
	// in persistence.xml. In these XML files you'll see the "jdbc/<service name>"
	// JNDI name used.

	private EntityManager getEm() {
		
		InitialContext ic;
		
		try {
			ic = new InitialContext();
			return (EntityManager) ic.lookup("java:comp/env/openjpa-order/entitymanager");
			
		} catch (NamingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private UserTransaction getUserTransaction() {
		
		InitialContext ic;
		
		try {
			ic = new InitialContext();
			return (UserTransaction) ic.lookup("java:comp/UserTransaction");
			
		} catch (NamingException e) {
			e.printStackTrace();
		}
		
		return null;
	}
}