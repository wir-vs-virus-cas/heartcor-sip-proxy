/*
 * Created on 16.07.2008 by Martin.Kirchner (mailto:Martin.Kirchner@cas.de)
 * 
 * Project: teamCRM Name of compilation unit: EIMClient.java Primary type of this compilation
 * unit: EIMClient
 * 
 * This software is confidential and proprietary information of CAS Software AG. You shall not
 * disclose such Confidential Information and shall use it only in accordance with the terms of
 * the license agreement you entered into with CAS Software AG.
 */
package com.example.restfulservice;

import java.util.Collections;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;

import de.cas.open.authentication.ChangeAbleAuthenticationProvider;
import de.cas.open.eimInterfaceProvider.EIMInterfaceFactory;
import de.cas.open.server.api.business.GetAsynchronousOperationStatusRequest;
import de.cas.open.server.api.business.GetAsynchronousOperationStatusResponse;
import de.cas.open.server.api.business.RunAsynchronouslyRequest;
import de.cas.open.server.api.business.RunAsynchronouslyResponse;
import de.cas.open.server.api.eiminterface.EIMInterface;
import de.cas.open.server.api.exceptions.BusinessException;
import de.cas.open.server.api.exceptions.DataLayerException;
import de.cas.open.server.api.types.RequestObject;
import de.cas.server.webservice.runtime.porttype.DynamicEIMServicePortTypeGenerator;

/**
 * A client for an EIM server.
 * 
 * @author Martin.Kirchner (<a href="mailto:Martin.Kirchner@cas.de">Martin.Kirchner@cas.de</a>)
 * @since 16.07.2008
 */
public abstract class EIMClient {


	/** The webservice facade. */
	protected EIMInterface eimInterface;

	/** Provides credentials for the webservice. */
	protected ChangeAbleAuthenticationProvider authenticationProvider = new ChangeAbleAuthenticationProvider();

	private static final Logger LOG = Logger.getLogger(CreateNewUser.class.getName());
	/**
	 * Constructs a new EIMClient.
	 * 
	 * @param args
	 *           the username (including the client), the password, the communication type (RMI or WS), the product key and the URL
	 */
	public EIMClient(final String[] args) {

		//insert your ManagementUser here
		final String managementUser = "EIM";
		authenticationProvider.setUserName(managementUser);

		//use your management password here
		String managementPassword = "CAS";
		authenticationProvider.setPassword(managementPassword);

		final String url = getHost();
		
		//use a productkey valid for you
		final String productKey = "";
		
		//is needed so that this works as well with machines that do not have a valid external ssl-certificate
		final boolean sslIgnoreInvalidHost = true;

		try {
			DynamicEIMServicePortTypeGenerator.setAnnotatedPortTypeToEIMService(Collections.EMPTY_SET);
			LOG.info(url + " SmartWe host url");
			this.eimInterface = new EIMInterfaceFactory(url, sslIgnoreInvalidHost, authenticationProvider)
				.getEIMInterface(productKey);
		} catch (final Exception e) {
			e.printStackTrace();
			System.exit(1);
		}

	}


	/**
	 * @return The host 
	 * @since 30.01.2012
	 */
	protected String getHost() {
		//insert host on which the smartwe is running
		return "https://beta.cas-future-labs.de/SmartWe/";
	}

	protected String executeAsync(final RequestObject req) throws BusinessException, DataLayerException {
		final RunAsynchronouslyResponse runAsyncResp = (RunAsynchronouslyResponse) eimInterface
			.execute(new RunAsynchronouslyRequest(req));
		final String operationGUID = runAsyncResp.getAsynchronousOperationGUID();
		System.out.println("AsynchronousOperationGUID: " + operationGUID);
		return operationGUID;
	}

	protected GetAsynchronousOperationStatusResponse getAsyncStatus(final String operationGuid) throws BusinessException, DataLayerException {
		final GetAsynchronousOperationStatusResponse getStatusResp = (GetAsynchronousOperationStatusResponse) eimInterface
			.execute(new GetAsynchronousOperationStatusRequest(operationGuid));
		return getStatusResp;
	}

	/**
	 * Loads logging configuration.
	 */
	private static void initLogging() {
		//let us do logging
	}

}
