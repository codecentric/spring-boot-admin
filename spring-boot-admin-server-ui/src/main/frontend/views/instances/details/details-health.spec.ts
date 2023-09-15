import {beforeEach, describe, expect, it} from "vitest";
import Application from "@/services/application";
import {applications} from "@/mocks/applications/data";
import {server} from "@/mocks/server";
import {rest} from "msw";
import DetailsHealth from "@/views/instances/details/details-health.vue";
import {render} from "@/test-utils";
import {screen, waitFor} from "@testing-library/vue";
import userEvent from "@testing-library/user-event";

describe('DetailsHealth', () => {
    describe('Health Group', () => {
        beforeEach(() => {
            server.use(
                rest.get('/instances/:instanceId/actuator/health', (req, res, ctx) => {
                    return res(ctx.status(200), ctx.json({
                            "instance": "UP",
                            "groups": ["liveness"]
                        }
                    ));
                }),
                rest.get('/instances/:instanceId/actuator/health/liveness', (req, res, ctx) => {
                    return res(ctx.status(200), ctx.json({
                            "status": "UP",
                            "details": {
                                "disk": {"status": "UNKNOWN"},
                                "database": {"status": "UNKNOWN"}
                            }
                        }
                    ));
                })
            );
        })

        it('should display groups as part of health section', async () => {
            const application = new Application(applications[0]);
            const instance = application.instances[0];

            render(DetailsHealth, {
                props: {
                    instance,
                }
            });

            await waitFor(() => expect(screen.queryByRole('status')).not.toBeInTheDocument());

            expect(await screen.findByRole('button', {name: /instances.details.health_group.title: liveness/})).toBeVisible();
        });

        it('health groups are toggleable, when details are available', async () => {
            const application = new Application(applications[0]);
            const instance = application.instances[0];
            instance.statusInfo = {};

            render(DetailsHealth, {
                props: {
                    instance,
                }
            });

            await waitFor(() => expect(screen.queryByRole('status')).not.toBeInTheDocument());

            let button = screen.queryByRole('button', {name: /instances.details.health_group.title: liveness/});
            expect(button).toBeVisible();

            expect(screen.queryByLabelText('disk')).toBeNull();
            expect(screen.queryByLabelText('database')).toBeNull();

            await userEvent.click(button);

            expect(screen.queryByLabelText('disk')).toBeDefined();
            expect(screen.queryByLabelText('database')).toBeDefined();
        });
    });
});

