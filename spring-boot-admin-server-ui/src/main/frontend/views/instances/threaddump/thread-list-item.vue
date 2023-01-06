<template>
  <table
    class="table-auto table-wide table-striped w-full my-2 border shadow bg-white"
  >
    <colgroup>
      <col class="w-60" />
    </colgroup>
    <tr>
      <td class="label" v-text="$t('instances.threaddump.thread_id')" />
      <td v-text="thread.threadId" />
    </tr>
    <tr>
      <td class="label" v-text="$t('instances.threaddump.thread_name')" />
      <td v-text="thread.threadName" />
    </tr>
    <template v-if="details !== null">
      <tr>
        <td class="label" v-text="$t('instances.threaddump.thread_state')" />
        <td v-text="details.threadState" />
      </tr>
      <tr>
        <td
          class="label"
          v-text="$t('instances.threaddump.thread_details_blocked_count')"
        />
        <td v-text="details.blockedCount" />
      </tr>
      <tr>
        <td
          class="label"
          v-text="$t('instances.threaddump.thread_details_blocked_time')"
        />
        <td v-text="details.blockedTime" />
      </tr>
      <tr>
        <td
          class="label"
          v-text="$t('instances.threaddump.thread_details_waited_count')"
        />
        <td v-text="details.waitedCount" />
      </tr>
      <tr>
        <td
          class="label"
          v-text="$t('instances.threaddump.thread_details_waited_time')"
        />
        <td v-text="details.waitedTime" />
      </tr>
      <tr>
        <td
          class="label"
          v-text="$t('instances.threaddump.thread_details_lock_name')"
        />
        <td v-text="details.lockName" />
      </tr>
      <tr>
        <td
          class="label"
          v-text="$t('instances.threaddump.thread_details_lock_owner_id')"
        />
        <td v-text="details.lockOwnerId" />
      </tr>
      <tr>
        <td
          class="label"
          v-text="$t('instances.threaddump.thread_details_lock_owner_name')"
        />
        <td v-text="details.lockOwnerName" />
      </tr>
      <tr v-if="details.stackTrace.length > 0">
        <td colspan="2">
          <span class="label" v-text="$t('term.stacktrace')" />
          <div class="text-sm">
            <template
              v-for="(frame, idx) in details.stackTrace"
              :key="`frame-${thread.threadId}-${idx}`"
            >
              <div class="whitespace-pre font-mono">
                <span
                  v-text="
                    `${frame.className}.${frame.methodName}(${frame.fileName}:${frame.lineNumber})`
                  "
                />
                <sba-tag
                  v-if="frame.nativeMethod"
                  :key="`frame-${thread.threadId}-${idx}-native`"
                  value="native"
                />
              </div>
            </template>
          </div>
        </td>
      </tr>
    </template>
  </table>
</template>
<script>
export default {
  name: 'ThreadListItem',
  props: {
    thread: {
      type: Object,
      required: true,
    },
    details: {
      type: Object,
      required: true,
    },
  },
};
</script>
