import Application from '@/services/application';
import Instance from '@/services/instance';
import { sanitizeHtml } from '@/utils/sanitizeHtml';

export interface ActionHandler {
  restart(item: any): Promise<void>;

  unregister(item: any): Promise<void>;

  shutdown(item: any): Promise<void>;
}

export class InstanceActionHandler implements ActionHandler {
  constructor(
    private $sbaModal: any,
    private t: any,
    private notificationCenter: any,
  ) {}

  async unregister(item: Instance) {
    const isConfirmed = await this.$sbaModal.confirm(
      this.t('applications.actions.unregister'),
      sanitizeHtml(this.t('instances.unregister', { name: item.id })),
    );
    if (!isConfirmed) {
      return;
    }

    try {
      await item.unregister();
      this.notificationCenter.success(
        sanitizeHtml(
          this.t('instances.unregister_successful', { name: item.id }),
        ),
      );
    } catch (error) {
      this.notificationCenter.error(
        sanitizeHtml(
          this.t('instances.unregister_failed', {
            name: item.id || item.name,
            error: error.response.status,
          }),
        ),
      );
    }
  }

  async shutdown(item: Instance) {
    const isConfirmed = await this.$sbaModal.confirm(
      this.t('applications.actions.shutdown'),
      sanitizeHtml(this.t('instances.shutdown', { name: item.id })),
    );
    if (!isConfirmed) {
      return;
    }

    try {
      await item.shutdown();
      this.notificationCenter.success(
        sanitizeHtml(
          this.t('instances.shutdown_successful', { name: item.id }),
        ),
      );
    } catch (error) {
      this.notificationCenter.error(
        sanitizeHtml(
          this.t('instances.shutdown_failed', {
            name: item.id || item.name,
            error: error.response.status,
          }),
        ),
      );
    }
  }

  async restart(item: Instance) {
    const isConfirmed = await this.$sbaModal.confirm(
      this.t('applications.actions.restart'),
      sanitizeHtml(this.t('instances.restart', { name: item.id })),
    );
    if (!isConfirmed) {
      return;
    }

    try {
      await item.restart();
      this.notificationCenter.success(
        sanitizeHtml(this.t('instances.restarted', { name: item.id })),
      );
    } catch (error) {
      this.notificationCenter.error(
        sanitizeHtml(
          this.t('instances.restart_failed', {
            name: item.id || item.name,
            error: error.response.status,
          }),
        ),
      );
    }
  }
}

export class ApplicationActionHandler implements ActionHandler {
  constructor(
    private $sbaModal: any,
    private t: any,
    private notificationCenter: any,
  ) {}

  async restart(application: Application) {
    const isConfirmed = await this.$sbaModal.confirm(
      this.t('applications.actions.restart'),
      sanitizeHtml(this.t('applications.restart', { name: application.name })),
    );
    if (!isConfirmed) {
      return;
    }

    try {
      await application.restart();
      this.notificationCenter.success(
        sanitizeHtml(
          this.t('applications.restarted', { name: application.name }),
        ),
      );
    } catch (error) {
      this.notificationCenter.error(
        sanitizeHtml(
          this.t('applications.restart_failed', {
            name: application.name,
            error: error.response.status,
          }),
        ),
      );
    }
  }

  async shutdown(application: Application) {
    const isConfirmed = await this.$sbaModal.confirm(
      this.t('applications.actions.shutdown'),
      sanitizeHtml(this.t('applications.shutdown', { name: application.name })),
    );
    if (!isConfirmed) {
      return;
    }

    try {
      await application.shutdown();
      this.notificationCenter.success(
        sanitizeHtml(
          this.t('applications.shutdown_successful', {
            name: application.name,
          }),
        ),
      );
    } catch (error) {
      this.notificationCenter.error(
        sanitizeHtml(
          this.t('applications.shutdown_failed', {
            name: application.name,
            error: error.response.status,
          }),
        ),
      );
    }
  }

  async unregister(application: Application) {
    const isConfirmed = await this.$sbaModal.confirm(
      this.t('applications.actions.unregister'),
      sanitizeHtml(
        this.t('applications.unregister', { name: application.name }),
      ),
    );
    if (!isConfirmed) {
      return;
    }

    try {
      await application.unregister();
      this.notificationCenter.success(
        sanitizeHtml(
          this.t('applications.unregister_successful', {
            name: application.name,
          }),
        ),
      );
    } catch (error) {
      this.notificationCenter.error(
        sanitizeHtml(
          this.t('applications.unregister_failed', {
            name: application.name,
            error: error.response.status,
          }),
        ),
      );
    }
  }
}
