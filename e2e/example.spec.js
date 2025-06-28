const { test, expect } = require('@playwright/test');

test.beforeAll(async ({ browser }) => {
  const page = await browser.newPage();
  // Go to login page
  await page.goto('http://localhost:8080?controller=auth&action=login');
  // Fill in credentials and submit
  await page.fill('input[name="username"]', 'admin');
  await page.fill('input[name="password"]', 'pass');
  await page.click('input[type="submit"]');
  // Go to reset URL
  await page.goto('http://localhost:8080?controller=admin&action=reset');
  await page.close();
});

//test('homepage has expected title', async ({ page }) => {
//  await page.goto('http://localhost:8080');
//  await expect(page).toHaveTitle(/Pheide/);
//});
//
//test('general navigation switches tab content', async ({ page }) => {
//  await page.goto('http://localhost:8080');
//
//  console.log('Checking initial tab content');
//  const tabContent1 = await page.locator('#tabContent').innerText();
//  expect(tabContent1.trim()).toBe('Main content 1');
//
//  console.log('Clicking #mill tab link');
//  await page.hover('#mill');
//  await Promise.all([
//    page.waitForNavigation(),
//    page.click('#mill a')
//  ]);
//
//  console.log('Checking tab content after navigation');
//  const tabContent2 = await page.locator('#tabContent').innerText();
//  expect(tabContent2.trim()).toBe('mill tab content 1');
//});
//
//test('general content editing updates tab content', async ({ page }) => {
//  console.log('Logging in');
//  await page.goto('http://localhost:8080?controller=auth&action=login');
//  await page.fill('input[name="username"]', 'admin');
//  await page.fill('input[name="password"]', 'pass');
//  await Promise.all([
//    page.waitForNavigation(),
//    page.click('input[type="submit"]')
//  ]);
//  console.log('Navigating to main page');
//  await page.goto('http://localhost:8080');
//
//  console.log('Filling in new content in textarea');
//  console.log('Double clicking #tabContent to enter edit mode');
//  await page.dblclick('#tabContent');
//
//  console.log('Filling in new content in textarea');
//  const newContent = 'Edited main content!';
//  await page.fill('#tabContent_edit textarea', newContent);
//
//  console.log('Submitting edited content');
//  await Promise.all([
//    page.waitForNavigation(),
//    page.click('#tabContent_edit input[type="submit"]')
//  ]);
//
//  console.log('Verifying updated content');
//  const updatedContent = await page.locator('#tabContent').innerText();
//  expect(updatedContent.trim()).toBe(newContent);
//});

test('can create a new tab', async ({ page }) => {
  console.log('Logging in');
    await page.goto('http://localhost:8080?controller=auth&action=login');
    await page.fill('input[name="username"]', 'admin');
    await page.fill('input[name="password"]', 'pass');
    await Promise.all([
      page.waitForNavigation(),
      page.click('input[type="submit"]')
    ]);
    console.log('Navigating to main page');
      await page.goto('http://localhost:8080');

  console.log('Clicking "+" to add a new tab');
  await Promise.all([
    page.waitForNavigation(),
    page.click('#cats li a[href*="controller=tab&action=new"]')
  ]);

  console.log('Filling in new tab');
  const newTabTitle = 'My New Tab';
  await page.fill('#cats li.activeTab input[name="title"]', newTabTitle);

  console.log('Submitting new tab form');
  await Promise.all([
    page.waitForNavigation(),
        page.click('#cats li.activeTab input[type="submit"]')
  ]);

  console.log('Verifying the active tab is the new one');
    const activeTabText = await page.locator('#cats li.activeTab #tabTitle').innerText();
    expect(activeTabText).toContain(newTabTitle);

  console.log('Clicking the delete (x) button on the new tab');
  await Promise.all([
    page.waitForNavigation(),
    page.click('#cats li.activeTab a.tabAction[href*="action=delete"]')
  ]);

  console.log('Verifying new tab is removed from tab list');
  const tabListAfterDelete = await page.locator('#cats').innerText();
  expect(tabListAfterDelete).not.toContain(newTabTitle);
});
