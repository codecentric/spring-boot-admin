export interface ConditionDetail {
  condition: string;
  message: string;
}

export interface ConditionalBean {
  name: string;
  matched?: ConditionDetail[];
  notMatched?: ConditionDetail[];
}
