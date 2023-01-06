export default {
  events: [
    {
      timestamp: '2017-03-14T22:59:58+0000',
      principal: 'user',
      type: 'AUTHENTICATION_FAILURE',
      data: {
        details: {
          remoteAddress: '0:0:0:0:0:0:0:1',
          sessionId: null,
        },
        type: 'org.springframework.security.authentication.BadCredentialsException',
        message: 'Bad credentials',
      },
    },
    {
      timestamp: '2017-03-14T23:00:07+0000',
      principal: 'user',
      type: 'AUTHENTICATION_SUCCESS',
      data: {
        details: {
          remoteAddress: '0:0:0:0:0:0:0:1',
          sessionId: null,
        },
      },
    },
  ],
};
