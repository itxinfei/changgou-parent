package com.changgou.goods.controller;

import com.changgou.entity.Result;
import com.changgou.entity.StatusCode;
import com.changgou.goods.pojo.Goods;
import com.changgou.goods.pojo.Spu;
import com.changgou.goods.service.SpuService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/spu")
@CrossOrigin
public class SpuController {

    @Autowired
    private SpuService spuService;

    /**
     * 分页加条件查询商品列表
     * @param page
     * @param size
     * @param spu
     * @return
     */
     @PostMapping("/findPage/{page}/{size}")
     public Result<List<Spu>> findPage(@PathVariable(value = "page")Integer page,
                                       @PathVariable(value = "size") Integer size,
                                       @RequestBody Spu spu){
         PageInfo<Spu> pageInfo = spuService.findPage(spu, page, size);
         return new Result<>(true, StatusCode.OK,"分页加条件查询商品列表成功",pageInfo);
     }

    /**
     * 还原删除商品
     * @param spuId
     * @return
     */
    @GetMapping("/Delete/{spuId}")
    public Result Delete(@PathVariable(value = "spuId")Long spuId){
        spuService.Delete(spuId);
        return new Result(true,StatusCode.OK,"物理删除商品操作成功！！！");
    }

    /**
     * 还原删除商品
     * @param spuId
     * @return
     */
    @GetMapping("/restore/{spuId}")
    public Result restore(@PathVariable(value = "spuId")Long spuId){
        spuService.restore(spuId);
        return new Result(true,StatusCode.OK,"还原删除商品操作成功！！！");
    }


    /**
     * 逻辑删除商品
     * @param spuId
     * @return
     */
    @GetMapping("/logicDelete/{spuId}")
    public Result logicDelete(@PathVariable(value = "spuId")Long spuId){
        spuService.logicDelete(spuId);
        return new Result(true,StatusCode.OK,"逻辑删除商品操作成功！！！");
    }


    /**
     * 商品批量上下架操作
     * @param isMarketable
     * @param ids
     * @return
     */
    @PostMapping("/isShows/{isMarketable}")
    public Result isShows(@PathVariable(value = "isMarketable")String isMarketable,
                          @RequestBody Long[] ids){
        spuService.isShows(ids,isMarketable);
        return new Result(true,StatusCode.OK,"批量上下架操作成功！！！");
    }

    /**
     * 批量上架
     * @param ids
     * @return  上架商品数量
     */
    @PostMapping("/putMany")
    public Result putMany(@RequestBody Long[] ids){
        int count = spuService.putMany(ids);
        return new Result(true,StatusCode.OK,"批量上架了"+count+"件商品");
    }


    /**
     * 批量下架
     * @param ids
     * @return 下架商品数量
     */
    @PostMapping("/pullMany")
    public Result pullMany(@RequestBody Long[] ids){
        int count = spuService.pullMany(ids);
        return new Result(true,StatusCode.OK,"批量下架了"+count+"件商品");
    }


    /**
     * 商品上下架
     * @param id
     * @param isMarketable
     * @return
     */
    @GetMapping("/isShoe/{id}/{isMarketable}")
    public Result isShow(@PathVariable(value = "id")Long id,
                         @PathVariable(value = "isMarketable") String isMarketable){
        spuService.isShow(id,isMarketable);
        return new Result(true,StatusCode.OK,"商品上下架操作成功");
    }


    /**
     * 上架架商品
     * @param spuId
     */
    @GetMapping("/put/{spuId}")
    public Result put(@PathVariable(value = "spuId")Long spuId){
        spuService.put(spuId);
        return new Result(true,StatusCode.OK,"商品上架架成功");
    }


    /**
     * 下架商品
     * @param spuId
     */
    @GetMapping("/pull/{spuId}")
    public Result pull(@PathVariable(value = "spuId")Long spuId){
        spuService.pull(spuId);
        return new Result(true,StatusCode.OK,"商品下架成功");
    }

    /**
     * 商品审核
     * @param spuId
     * @return
     */
    @GetMapping("/audit/{spuId}")
    public Result audit(@PathVariable(value = "spuId") Long spuId){
        spuService.audit(spuId);
        return new Result(true,StatusCode.OK,"商品审核通过！！！");
    }

    /**
     * 根据id查询返回一个Goods对象
     * @param spuId
     * @return
     */
    @GetMapping("/goods/{spuId}")
    public Result<Goods> findGoodsById(@PathVariable(value = "spuId") Long spuId){
        Goods goods = spuService.findGoodsById(spuId);
        return new Result<Goods>(true,StatusCode.OK,"查询成功",goods);
    }


    /**
     * 修改商品并保存
     * @param goods
     * @return
     */
    @PostMapping("/edit")
    public Result Edit(@RequestBody Goods goods){
        spuService.Edit(goods);
        return new Result(true,StatusCode.OK,"修改商品并保存成功");
    }


    /**
     * 保存商品
     * @param goods
     * @return
     */
    @PostMapping("/save")
    public Result save(@RequestBody Goods goods){
        spuService.save(goods);
        return new Result(true,StatusCode.OK,"录入商品成功");
    }


    /***
     * Spu分页条件搜索实现
     * @param spu
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@RequestBody(required = false)  Spu spu, @PathVariable  int page, @PathVariable  int size){
        //调用SpuService实现分页条件查询Spu
        PageInfo<Spu> pageInfo = spuService.findPage(spu, page, size);
        return new Result(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * Spu分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result<PageInfo> findPage(@PathVariable  int page, @PathVariable  int size){
        //调用SpuService实现分页查询Spu
        PageInfo<Spu> pageInfo = spuService.findPage(page, size);
        return new Result<PageInfo>(true,StatusCode.OK,"查询成功",pageInfo);
    }

    /***
     * 多条件搜索品牌数据
     * @param spu
     * @return
     */
    @PostMapping(value = "/search" )
    public Result<List<Spu>> findList(@RequestBody(required = false)  Spu spu){
        //调用SpuService实现条件查询Spu
        List<Spu> list = spuService.findList(spu);
        return new Result<List<Spu>>(true,StatusCode.OK,"查询成功",list);
    }

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    public Result delete(@PathVariable Long id){
        //调用SpuService实现根据主键删除
        spuService.delete(id);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 修改Spu数据
     * @param spu
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    public Result update(@RequestBody  Spu spu,@PathVariable Long id){
        //设置主键值
        spu.setId(id);
        //调用SpuService实现修改Spu
        spuService.update(spu);
        return new Result(true,StatusCode.OK,"修改成功");
    }

    /***
     * 新增Spu数据
     * @param spu
     * @return
     */
    @PostMapping
    public Result add(@RequestBody   Spu spu){
        //调用SpuService实现添加Spu
        spuService.add(spu);
        return new Result(true,StatusCode.OK,"添加成功");
    }

    /***
     * 根据ID查询Spu数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Spu> findById(@PathVariable(value = "id") Long id){
        //调用SpuService实现根据主键查询Spu
        Spu spu = spuService.findById(id);
        return new Result<Spu>(true,StatusCode.OK,"查询成功",spu);
    }

    /***
     * 查询Spu全部数据
     * @return
     */
    @GetMapping
    public Result<List<Spu>> findAll(){
        //调用SpuService实现查询所有Spu
        List<Spu> list = spuService.findAll();
        return new Result<List<Spu>>(true, StatusCode.OK,"查询成功",list) ;
    }
}
