package webiste.lhc.heron.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;
import webiste.lhc.heron.commo.RoleConstant;
import webiste.lhc.heron.commo.enums.MenuEnum;
import webiste.lhc.heron.mapper.MenuMapper;
import webiste.lhc.heron.model.Menu;
import webiste.lhc.heron.service.MenuService;
import webiste.lhc.heron.util.Assert;
import webiste.lhc.heron.vo.ZtreeVo;

import java.util.*;

/**
 * @ProjectName: heron
 * @Package: webiste.lhc.heron.service.impl
 * @ClassName: MenuServiceImpl
 * @Author: lhc
 * @Description: TODO
 * @Date: 2020/8/16 下午 05:24
 */
@Slf4j
@Service
public class MenuServiceImpl implements MenuService {


    @Autowired
    private MenuMapper menuMapper;


    /**
     * 先获取目录，在获取菜单
     *
     * @param userId
     * @return
     */
    @Override
    public List<Menu> getMenuByUserId(long userId) {
        // 所有菜单
        if (RoleConstant.ADMIN_USER_ID == userId) {
            return getAllMenus(null);
        }
        List<Long> menuIdList = menuMapper.listMenuIdByUserId(userId);
        return getAllMenus(menuIdList);
    }


    @Override
    public List<Menu> listMenuBYType(long pid) {
        Example example = new Example(Menu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("parentId", pid);
        List<String> list = new ArrayList<>(2);
        list.add(MenuEnum.DIR.val());
        list.add(MenuEnum.MENU.val());
        criteria.andIn("type", list);
        criteria.andEqualTo("isDelete", false);
        return menuMapper.selectByExample(example);
    }

    @Override
    public Menu getMenuById(long id) {
        return menuMapper.selectByPrimaryKey(id);
    }

    @Override
    public void delMenuById(long id) {
        Menu menu = new Menu();
        menu.setParentId(id);
        menu.setIsDelete(false);
        int menuCount = menuMapper.selectCount(menu);
        log.info("id:{}; menuCount:{}", id, menuCount);
        Assert.stat(menuCount > 0, "请先删除子菜单");
        Menu menu1 = new Menu();
        menu1.setId(id);
        menu1.setIsDelete(true);
        menu1.setUpdateTime(new Date());
        menuMapper.updateByPrimaryKeySelective(menu1);

    }

    @Override
    public List<ZtreeVo> listDataToTree() {
        List<ZtreeVo> ztreeVos = new ArrayList<>();
        ztreeVos.add(new ZtreeVo(0L,999L,"根节点", true));
        List<Menu> dirList = menuMapper.listTree(0, MenuEnum.DIR.val());
        if (CollectionUtils.isEmpty(dirList)) {
            return Collections.emptyList();
        }
        for (Menu dir : dirList) {
            int count = menuMapper.menuCount(dir.getId());
            ZtreeVo vo = new ZtreeVo();
            vo.setId(dir.getId());
            vo.setpId(dir.getParentId());
            vo.setName(dir.getMenuName());
            vo.setParent(count > 0);
            ztreeVos.add(vo);

            if (count > 0) {
                List<Menu> menuList = menuMapper.listTree(dir.getId(), MenuEnum.MENU.val());
                if (!CollectionUtils.isEmpty(menuList)) {
                    for (Menu menu : menuList) {
                        ztreeVos.add(new ZtreeVo(menu.getId(), menu.getParentId(), menu.getMenuName(), false));
                    }
                }
            }
        }

        return ztreeVos;
    }

    @Override
    public void insertMenu(Menu menu) {
        menu.setCreateTime(new Date());
        menuMapper.insert(menu);
    }

    @Override
    public void updateMenu(Menu menu) {
        menu.setUpdateTime(new Date());
        menuMapper.updateByPrimaryKey(menu);
    }

    @Override
    public List<Map<String, Object>> listMenu() {
        Example example = new Example(Menu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("isDelete", false);
        example.selectProperties("id", "parentId", "menuName", "url", "permission", "isDelete", "type");
        return menuMapper.listMenus();
    }


    /**
     * 若menuIdList为空表示超级管理员，否则表示特定用户
     *
     * @param menuIdList
     * @return
     */
    private List<Menu> listMenu(Long parentId, List<Long> menuIdList) {
        List<Menu> dirList = listParent(parentId);
        if (CollectionUtils.isEmpty(menuIdList)) {
            return dirList;
        }
        List<Menu> menus = new ArrayList<>();
        for (Menu menu : dirList) {
            if (menuIdList.contains(menu.getId())) {
                menus.add(menu);
            }
        }
        return menus;
    }

    public List<Menu> getAllMenus(List<Long> idList) {
        List<Menu> parents = listMenu(0L, idList);
        listMenus(parents, idList);
        return parents;
    }

    /**
     * 获取所有parentId的子节点
     *
     * @param parentId
     * @return
     */
    private List<Menu> listParent(long parentId) {
        return menuMapper.getParent(parentId);
    }

    private List<Menu> listMenus(List<Menu> dirList, List<Long> menuIdList) {
        List<Menu> subMenuList = new ArrayList<>();
        for (Menu menu : dirList) {
            if ("D".equals(menu.getType())) {
                menu.setChild(listMenus(listMenu(menu.getId(), menuIdList), menuIdList));
            }
            subMenuList.add(menu);
        }
        return subMenuList;
    }

}
