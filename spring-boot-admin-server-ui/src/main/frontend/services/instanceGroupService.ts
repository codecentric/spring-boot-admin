import Instance from "@/services/instance";
import {groupBy, sortBy, transform} from "lodash-es";
import Application from "@/services/application";
import {useApplicationStore} from "@/composables/useApplicationStore";

const groupingFunctions = {
    'application': (instance: Instance) => instance.registration.name,
    'group': (instance: Instance) => instance.registration.metadata?.['group'] ?? "term.no_group",
}

export type GroupingType = keyof typeof groupingFunctions;
export const isGroupingType = (grouping: string = ""): grouping is GroupingType => {
    return Object.keys(groupingFunctions).includes(grouping);
}

export type InstancesListItem = {
    name: string;
    statusKey?: string;
    status?: string;
    instances: Instance[];
}

export const groupApplicationsBy = (applications: Application[], groupingFunction: GroupingType) => {
    const {applicationStore} = useApplicationStore();
    const instances = applications.flatMap(application => application.instances);
    const groupedInstances = groupInstancesBy(instances, groupingFunction);

    if (groupingFunction === 'application') {
        return groupedInstances
            .map(item => {
                return {
                    ...applicationStore.findApplicationByInstanceId(item.instances[0].id),
                    ...item,
                }
            });
    }

    return groupedInstances
        .map(item => {
            return {
                status: getStatus(item.instances),
                ...item,
            }
        });
}

function getStatus(instances: Instance[]) {
    if (instances.every(instance => instance.statusInfo.status === 'DOWN')) {
        return 'DOWN';
    }
    if (instances.every(instance => instance.statusInfo.status === 'UP')) {
        return 'UP';
    }
    return "RESTRICTED";
}

export const groupInstancesBy = (instances: Instance[], groupingFunction: GroupingType) => {
    const grouped = groupBy<Instance>(
        instances,
        groupingFunctions[groupingFunction]
    );

    const list = transform<Instance[], InstancesListItem[]>(
        grouped,
        (result, instances, name) => {
            result.push({
                name,
                instances: sortBy(instances, [
                    (instance) => instance.registration.name,
                ]),
            });
        }, []);

    return sortBy(list, [(item) => item.status]);
}

