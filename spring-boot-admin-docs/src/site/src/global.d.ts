export {};

declare global {
  type SpringPropertyDefinition = {
    name: string;
    description: string;
    type?: string;
    defaultValue?: string;
  }
}
