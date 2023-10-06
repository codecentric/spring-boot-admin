import Instance from "@/services/instance";
import {groupBy, sortBy, transform} from "lodash-es";
import Application from "@/services/application";

const groupingFunctions = {
  'application': (instance: Instance) => instance.registration.name,
  'group': (instance: Instance) => instance.registration.metadata?.['group'] ?? "term.no_group",
}

export type GroupingType = keyof typeof groupingFunctions;

export type InstancesListType = {
  name?: string;
  statusKey?: string;
  status?: string;
  instances?: Instance[];
  applications?: Application[];
}

export const groupApplicationsBy = (applications: Application[], groupingFunction: GroupingType) => {
  const instances = applications.flatMap(application => application.instances);
  return groupInstancesBy(instances, groupingFunction);
}

export const groupInstancesBy = (instances: Instance[], groupingFunction: GroupingType) => {
  const grouped = groupBy<Instance>(
    instances,
    groupingFunctions[groupingFunction]
  );

  const list = transform<Instance[], InstancesListType[]>(
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

