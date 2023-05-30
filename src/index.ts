import { NativeModulesProxy, EventEmitter, Subscription } from 'expo-modules-core';

// Import the native module. On web, it will be resolved to ExpoPdfHelpers.web.ts
// and on native platforms to ExpoPdfHelpers.ts
import ExpoPdfHelpersModule from './ExpoPdfHelpersModule';
import ExpoPdfHelpersView from './ExpoPdfHelpersView';
import { ChangeEventPayload, ExpoPdfHelpersViewProps } from './ExpoPdfHelpers.types';

// Get the native constant value.
export const PI = ExpoPdfHelpersModule.PI;

export function hello(): string {
  return ExpoPdfHelpersModule.hello();
}

export async function setValueAsync(value: string) {
  return await ExpoPdfHelpersModule.setValueAsync(value);
}

const emitter = new EventEmitter(ExpoPdfHelpersModule ?? NativeModulesProxy.ExpoPdfHelpers);

export function addChangeListener(listener: (event: ChangeEventPayload) => void): Subscription {
  return emitter.addListener<ChangeEventPayload>('onChange', listener);
}

export { ExpoPdfHelpersView, ExpoPdfHelpersViewProps, ChangeEventPayload };
