# Enhancement Proposal: Granular URL Control for Spring Boot Admin

## Current Implementation Status
✅ **COMPLETE** - Issue #4338 has been successfully implemented in PR #3757 with the ability to hide all service URLs globally or per-instance.

## Enhancement Proposal: Granular URL Control

### Problem Statement
The current implementation hides all URLs (service, management, health) together. However, there are scenarios where users might want to:
- Hide service URLs (which might return 404) but keep management URLs visible
- Hide management URLs for security reasons but keep health URLs visible
- Have different visibility rules for different URL types

### Proposed Enhancement

#### 1. New Metadata Properties
Add granular control through metadata properties:

```yaml
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            hide-service-url: "true"      # Hide only service URL
            hide-management-url: "true"   # Hide only management URL  
            hide-health-url: "true"       # Hide only health URL
            hide-url: "true"              # Hide all URLs (existing behavior)
```

#### 2. New Server Configuration
Add granular server-side configuration:

```yaml
spring:
  boot:
    admin:
      ui:
        hide-instance-url: true           # Hide all URLs (existing)
        hide-service-url: true            # Hide only service URLs
        hide-management-url: true         # Hide only management URLs
        hide-health-url: true             # Hide only health URLs
```

#### 3. Enhanced Frontend Logic
Update the `Instance` service to support granular control:

```typescript
class Instance {
  showServiceUrl() {
    const sbaConfig = useSbaConfig();
    if (sbaConfig.uiSettings.hideInstanceUrl || sbaConfig.uiSettings.hideServiceUrl) {
      return false;
    }
    const hideServiceUrlMetadata = this.registration.metadata?.['hide-service-url'];
    const hideUrlMetadata = this.registration.metadata?.['hide-url'];
    return hideServiceUrlMetadata !== 'true' && hideUrlMetadata !== 'true';
  }

  showManagementUrl() {
    const sbaConfig = useSbaConfig();
    if (sbaConfig.uiSettings.hideInstanceUrl || sbaConfig.uiSettings.hideManagementUrl) {
      return false;
    }
    const hideManagementUrlMetadata = this.registration.metadata?.['hide-management-url'];
    const hideUrlMetadata = this.registration.metadata?.['hide-url'];
    return hideManagementUrlMetadata !== 'true' && hideUrlMetadata !== 'true';
  }

  showHealthUrl() {
    const sbaConfig = useSbaConfig();
    if (sbaConfig.uiSettings.hideInstanceUrl || sbaConfig.uiSettings.hideHealthUrl) {
      return false;
    }
    const hideHealthUrlMetadata = this.registration.metadata?.['hide-health-url'];
    const hideUrlMetadata = this.registration.metadata?.['hide-url'];
    return hideHealthUrlMetadata !== 'true' && hideUrlMetadata !== 'true';
  }

  // Keep existing method for backward compatibility
  showUrl() {
    return this.showServiceUrl() || this.showManagementUrl() || this.showHealthUrl();
  }
}
```

#### 4. Updated UI Components
Modify UI components to use granular methods:

```vue
<!-- InstancesList.vue -->
<template v-if="instance.showServiceUrl()">
  <a :href="instance.registration.serviceUrl" @click.stop>
    {{ instance.registration.serviceUrl }}
  </a>
</template>

<!-- details-nav.vue -->
<sba-button v-if="instance.showServiceUrl()" @click="openLink(instance.registration.serviceUrl)">
  Service
</sba-button>

<sba-button v-if="instance.showManagementUrl()" @click="openLink(instance.registration.managementUrl)">
  Management
</sba-button>

<sba-button v-if="instance.showHealthUrl()" @click="openLink(instance.registration.healthUrl)">
  Health
</sba-button>
```

### Configuration Hierarchy
The enhancement would follow this hierarchy (highest to lowest priority):

1. **Global server settings** (new granular properties)
2. **Global server setting** (`hide-instance-url` - existing)
3. **Per-instance metadata** (new granular properties)
4. **Per-instance metadata** (`hide-url` - existing)
5. **Default behavior** (show all URLs)

### Use Cases

#### Use Case 1: Hide Service URLs Only
```yaml
# Server config
spring:
  boot:
    admin:
      ui:
        hide-service-url: true

# Or per-instance
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            hide-service-url: "true"
```

#### Use Case 2: Hide Management URLs for Security
```yaml
# Server config
spring:
  boot:
    admin:
      ui:
        hide-management-url: true

# Or per-instance
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            hide-management-url: "true"
```

#### Use Case 3: Mixed Configuration
```yaml
# Server config - hide service URLs globally
spring:
  boot:
    admin:
      ui:
        hide-service-url: true

# Client config - hide management URLs for this instance
spring:
  boot:
    admin:
      client:
        instance:
          metadata:
            hide-management-url: "true"
            # Service URL will be hidden by server config
            # Health URL will be visible
```

### Backward Compatibility
- All existing configurations continue to work
- `hide-url: "true"` still hides all URLs
- `hide-instance-url: true` still hides all URLs
- New granular properties are additive to existing functionality

### Implementation Plan

#### Phase 1: Server Configuration
1. Add new properties to `AdminServerUiProperties`
2. Update `AdminServerUiAutoConfiguration`
3. Update `UiController` to pass new settings

#### Phase 2: Frontend Types and Configuration
1. Update `global.d.ts` with new properties
2. Update `sba-config.ts` with defaults
3. Add new methods to `Instance` service

#### Phase 3: UI Components
1. Update `InstancesList.vue` for granular service URL control
2. Update `details-nav.vue` for granular button control
3. Add comprehensive tests

#### Phase 4: Documentation
1. Update documentation with new properties
2. Add examples for different use cases
3. Update migration guide

### Benefits
1. **Flexibility**: Users can control URL visibility at a granular level
2. **Security**: Hide sensitive management endpoints while keeping health visible
3. **UX**: Hide non-functional service URLs while keeping useful management URLs
4. **Backward Compatibility**: Existing configurations continue to work
5. **Future-Proof**: Extensible design for additional URL types

### Testing Strategy
1. **Unit Tests**: Test each granular method independently
2. **Integration Tests**: Test configuration hierarchy
3. **UI Tests**: Verify correct button/link visibility
4. **Backward Compatibility Tests**: Ensure existing configs work

### Migration Path
- No breaking changes
- Existing configurations work unchanged
- New properties are optional
- Gradual adoption possible

This enhancement would provide users with much more control over URL visibility while maintaining backward compatibility with the existing implementation. 