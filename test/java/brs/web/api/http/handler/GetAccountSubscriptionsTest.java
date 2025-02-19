package brs.web.api.http.handler;

import brs.Account;
import brs.Blockchain;
import brs.Burst;
import brs.BurstException;
import brs.Subscription;
import brs.common.AbstractUnitTest;
import brs.common.QuickMocker;
import brs.common.QuickMocker.MockParam;
import brs.services.AliasService;
import brs.services.ParameterService;
import brs.services.SubscriptionService;
import brs.util.JSON;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static brs.web.api.http.common.Parameters.ACCOUNT_PARAMETER;
import static brs.web.api.http.common.Parameters.SUBSCRIPTIONS_RESPONSE;
import static brs.web.api.http.common.ResultFields.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Burst.class)
public class GetAccountSubscriptionsTest extends AbstractUnitTest {

  private ParameterService parameterServiceMock;
  private SubscriptionService subscriptionServiceMock;
  private AliasService aliasServiceMock;

  private GetAccountSubscriptions t;

  @Before
  public void setUp() {
    mockStatic(Burst.class);

    parameterServiceMock = mock(ParameterService.class);
    subscriptionServiceMock = mock(SubscriptionService.class);
    aliasServiceMock = mock(AliasService.class);

    Blockchain mockBlockchain = mock(Blockchain.class);
    when(Burst.getBlockchain()).thenReturn(mockBlockchain);

    t = new GetAccountSubscriptions(parameterServiceMock, subscriptionServiceMock, aliasServiceMock);
  }

  @Test
  public void processRequest() throws BurstException {
    final long userId = 123L;

    final HttpServletRequest req = QuickMocker.httpServletRequest(
      new MockParam(ACCOUNT_PARAMETER, userId)
    );

    final Account account = mock(Account.class);
    when(account.getId()).thenReturn(userId);
    when(parameterServiceMock.getAccount(eq(req))).thenReturn(account);

    final Subscription subscription = mock(Subscription.class);
    when(subscription.getId()).thenReturn(1L);
    when(subscription.getAmountNQT()).thenReturn(2L);
    when(subscription.getFrequency()).thenReturn(3);
    when(subscription.getTimeNext()).thenReturn(4);

    final Collection<Subscription> subscriptionIterator = this.mockCollection(subscription);
    when(subscriptionServiceMock.getSubscriptionsByParticipant(eq(userId))).thenReturn(subscriptionIterator);

    final JsonObject result = (JsonObject) t.processRequest(req);
    assertNotNull(result);

    final JsonArray resultSubscriptions = (JsonArray) result.get(SUBSCRIPTIONS_RESPONSE);
    assertNotNull(resultSubscriptions);
    assertEquals(1, resultSubscriptions.size());

    final JsonObject resultSubscription = (JsonObject) resultSubscriptions.get(0);
    assertNotNull(resultSubscription);

    assertEquals("" + subscription.getId(), JSON.getAsString(resultSubscription.get(ID_RESPONSE)));
    assertEquals("" + subscription.getAmountNQT(), JSON.getAsString(resultSubscription.get(AMOUNT_NQT_RESPONSE)));
    assertEquals(subscription.getFrequency(), JSON.getAsInt(resultSubscription.get(FREQUENCY_RESPONSE)));
    assertEquals(subscription.getTimeNext(), JSON.getAsInt(resultSubscription.get(TIME_NEXT_RESPONSE)));
  }

}
