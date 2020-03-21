/*
 * Created on 08.04.2008 by Martin.Kirchner (mailto:Martin.Kirchner@cas.de) Project: teamCRM Name of compilation unit:
 * CommandLineClient.java Primary type of this compilation unit: CommandLineClient This software is confidential and
 * proprietary information of CAS Software AG. You shall not disclose such Confidential Information and shall use it
 * only in accordance with the terms of the license agreement you entered into with CAS Software AG.
 */
package com.example.restfulservice;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.cas.open.server.api.exceptions.EIMException;
import de.cas.open.server.api.management.MISaveUserWithPasswordRequest;
import de.cas.open.server.api.types.UserType;
import de.cas.open.server.api.types.UserWithUserName;
import java.util.logging.Logger;

/**
 * A command line client for the EIM server. This tools changes a user's
 * password.
 *
 * @author Martin.Kirchner
 *         (<a href="mailto:Martin.Kirchner@cas.de">Martin.Kirchner@cas.de</a>)
 * @since 08.04.2008
 */
@RestController
public final class CreateNewUser extends EIMClient {
	private static final Logger LOG = Logger.getLogger(CreateNewUser.class.getName());

	/**
	 * Constructs a new ManagementInterfaceClient.
	 *
	 * @param args the commandline arguments
	 */
	public CreateNewUser(final String[] args) {
		super(args);
	}

	/**
	 * Executes the client.
	 * <p>
	 * <b>Author:</b> Martin.Kirchner
	 * (<a href="mailto:Martin.Kirchner@cas.de">Martin.Kirchner@cas.de</a>)
	 * </p>
	 *
	 * @param args the username (including the client), the password, the
	 *             communication type (RMI or WS), the product key and the URL
	 * @throws Exception if something went wrong
	 * @since 08.04.2008
	 */
	public static void main(final String[] args) throws Exception {

		new CreateNewUser(args).createNewUser("TakePart", "Neu.Benutzer26", "takepart@raumtaenzer.com", "Software1",
				"6UqEhGx6MFAuE9FYB29FPgcNlKoSAfMk6YA/XNv1jek=");
		System.out.println("Done.");
	}

	/**
	 * Resets a customer's password.
	 *
	 * @throws EIMException on error
	 * @since 07.01.2010
	 */
	@PostMapping("/createTakePartUser")
	public String createNewUser(@RequestParam(value = "mandant", defaultValue = "TakePart") String mandant,
			@RequestParam(value = "username", defaultValue = "Neu.Benutzer") String userName,
			@RequestParam(value = "email", defaultValue = "takepart@raumtaenzer.com") String email,
			@RequestParam(value = "password", defaultValue = "Software1") String password,
			@RequestParam(value = "produktKey", defaultValue = "") String partnerProduktKey) throws EIMException {

		// final String clientName = "TakePart";

		// final String userName = "default.benutzer";
		// final String email = "anuhari95@gmail.com";
		// final String password = "Software1";
		LOG.info("Start prep request...");
		if (null != partnerProduktKey) {
			if (partnerProduktKey.equals("6UqEhGx6MFAuE9FYB29FPgcNlKoSAfMk6YA/XNv1jek=")) {
				UserWithUserName user = new UserWithUserName();
				user.setLoginName(userName);
				user.setDisplayName(userName);
				user.setUserType(UserType.USER);
				user.setEmailAddress(email);
				user.setClient(mandant);

				MISaveUserWithPasswordRequest request = new MISaveUserWithPasswordRequest();
				request.setUserToSave(user);
				request.setPassword(password);

				LOG.info("Start exec request...");
				eimInterface.executeManagementOperation(request);
				return "New user " + userName + " created on " + mandant + " Mandant";
			} else {
				return "Partner Authentication failed";
			}
		} else {
			return "Partner Key Empty";
		}

		// response.getSavedUser();
	}

}
