import { describe, expect, it } from 'vitest';

import { useClassnameShortener } from '@/composables/useClassnameShortener';

describe('useClassnameShortener', () => {
  const { truncateClassname } = useClassnameShortener();

  it.each`
    given                                                             | expected
    ${'com.example.MyService'}                                        | ${'com.example.MyService'}
    ${'MyTopLevelClass'}                                              | ${'MyTopLevelClass'}
    ${'com.example.Outer$Inner'}                                      | ${'com.example.Outer$Inner'}
    ${'org.springframework.boot.web.embedded.tomcat.TomcatWebServer'} | ${'o.s.b.w.embedded.tomcat.TomcatWebServer'}
  `('$given => $expected', ({ given, expected }) => {
    expect(truncateClassname(given)).toEqual(expected);
  });
});
