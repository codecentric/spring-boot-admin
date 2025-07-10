# Issue #4338 Implementation Summary: Allow to disable the service URL on a client-basis

## Overview
Issue #4338 requested the ability to disable/hide service URLs on a client basis in Spring Boot Admin. This feature has been **successfully implemented** in PR #3757 and is now available in the current version.

## Feature Description
The feature allows users to hide service URLs in the Spring Boot Admin UI for security and usability reasons. This is particularly useful when:
- Some applications have UIs that are useful to access via service URL
- Other applications have no meaningful content at the service URL (returning 404)
- Users want to prevent accidental clicks on non-functional URLs

## Implementation Details

### 1. Server-Side Configuration
**Global Setting**: Hide all service URLs across the entire Spring Boot Admin instance
```yaml
spring:
  boot:
    admin:
      ui:
        hide-instance-url: true
```

**Property Location**: `AdminServerUiProperties.java` line 119
```java
/**
 * Show or hide URL of instances.
 */
private Boolean hideInstanceUrl = false;
```

### 2. Client-Side Configuration
**Per-Instance Setting**: Hide service URL for specific instances only
```yaml
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            hide-url: "true"
```

### 3. Frontend Implementation
**Core Logic**: `Instance.showUrl()` method in `instance.ts`
```typescript
showUrl() {
  const sbaConfig = useSbaConfig();
  if (sbaConfig.uiSettings.hideInstanceUrl) {
    return false;
  }

  const hideUrlMetadata = this.registration.metadata?.['hide-url'];
  return hideUrlMetadata !== 'true';
}
```

**UI Components Updated**:
- `InstancesList.vue`: Conditionally renders instance URLs
- `details-nav.vue`: Conditionally renders service/management/health URL buttons

### 4. Configuration Hierarchy
The feature follows a clear hierarchy:
1. **Global server setting** (`spring.boot.admin.ui.hide-instance-url`) takes precedence
2. **Per-instance metadata** (`hide-url: "true"`) works only when global setting is `false`
3. **Default behavior**: URLs are shown unless explicitly hidden

## Testing

### Unit Tests
Comprehensive test coverage in `instance.spec.ts`:
```typescript
test.each`
  hideInstanceUrl | metadataHideUrl | expectUrlToBeShownOnUI
  ${false}        | ${'true'}       | ${false}
  ${false}        | ${'false'}      | ${true}
  ${false}        | ${undefined}    | ${true}
  ${true}         | ${'true'}       | ${false}
  ${true}         | ${'false'}      | ${false}
`
```

### Test Scenarios Covered
- Global setting disabled, metadata hide-url = true → URL hidden
- Global setting disabled, metadata hide-url = false → URL shown
- Global setting disabled, no metadata → URL shown
- Global setting enabled, any metadata → URL hidden (global takes precedence)

## Documentation
The feature is documented in `spring-boot-admin-docs/src/site/docs/customize/01-customize_ui.md`:

```markdown
## Hide Service URL

To hide service URLs in Spring Boot Admin UI entirely, set the following property in your Server's configuration:

| Property name                            | Default | Usage                                                                                                              
|------------------------------------------|---------|--------------------------------------------------------------------------------------------------------------------
| `spring.boot.admin.ui.hide-instance-url` | `false` | Set to `true` to hide service URLs as well as actions that require them in UI (e.g. jump to /health or /actuator). 

If you want to hide the URL for specific instances only, you can set the `hide-url` property in the instance metadata
while registering a service.
When using Spring Boot Admin Client you can set the property `spring.boot.admin.client.metadata.hide-url=true` in the
corresponding config file. The value set in `metadata` does not have any effect, when the URLs are disabled in Server.
```

## Files Modified in PR #3757
1. **Server Configuration**:
   - `AdminServerUiProperties.java` - Added `hideInstanceUrl` property
   - `AdminServerUiAutoConfiguration.java` - Configuration integration
   - `UiController.java` - UI settings integration

2. **Frontend**:
   - `global.d.ts` - TypeScript type definitions
   - `sba-config.ts` - Default configuration
   - `instance.ts` - Core `showUrl()` logic
   - `InstancesList.vue` - UI component updates
   - `details-nav.vue` - Navigation component updates

3. **Documentation**:
   - `customize_ui.adoc` - Feature documentation

4. **Testing**:
   - `instance.spec.ts` - Comprehensive unit tests

## Usage Examples

### Example 1: Hide URLs for all instances
```yaml
# application.yml (Spring Boot Admin Server)
spring:
  boot:
    admin:
      ui:
        hide-instance-url: true
```

### Example 2: Hide URL for specific application
```yaml
# application.yml (Spring Boot Application)
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            hide-url: "true"
```

### Example 3: Mixed configuration
```yaml
# Server config - URLs shown by default
spring:
  boot:
    admin:
      ui:
        hide-instance-url: false

# Client 1 - URL hidden
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            hide-url: "true"

# Client 2 - URL shown (default behavior)
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            hide-url: "false"
```

## Benefits
1. **Security**: Prevents accidental access to non-functional URLs
2. **User Experience**: Cleaner UI when URLs are not meaningful
3. **Flexibility**: Both global and per-instance control
4. **Backward Compatibility**: Default behavior unchanged

## Status
✅ **COMPLETE** - The feature has been successfully implemented and is available in the current version of Spring Boot Admin.

## Related Issues/PRs
- **Issue**: #4338 - Feature request for client-basis URL hiding
- **PR**: #3757 - Implementation of the feature
- **Commit**: `eff73bb6` - "feat(#2538): implement configs to hide URLs in UI"

## Future Enhancements (Potential)
While the current implementation is complete and functional, potential future enhancements could include:

1. **Granular Control**: Hide specific URL types (service, management, health) separately
2. **Dynamic Updates**: Allow URL visibility to be toggled at runtime
3. **UI Indicators**: Visual indicators when URLs are hidden
4. **Audit Logging**: Track when URLs are accessed despite being hidden

## Conclusion
Issue #4338 has been **successfully resolved** with a comprehensive implementation that provides both global and per-instance control over service URL visibility. The feature is well-tested, documented, and ready for production use. 