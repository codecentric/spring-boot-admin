package de.codecentric.boot.admin.service;

import de.codecentric.boot.admin.model.Application;

import java.util.List;

/**
 * @author Robert Winkler <robert.winkler@telekom.de>
 */
public interface RegistryService {

    public Long registerApplication(Application app);

    public Application getApplicationById(Long id);

    public List<Application> getAllApplications();

    public void unregisterApplicationById(Long id);

    public boolean isRegistered(Long id);

    public void removeAllApplications();

    public Application updateApplication(Application app);
}
